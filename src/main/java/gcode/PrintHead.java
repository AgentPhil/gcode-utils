package gcode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.management.ApplicationService;
import app.management.Property;
import logging.Logger;
import logging.LoggerFactory;

public class PrintHead extends ApplicationService {

	private static Pattern G4_PATTERN = Pattern.compile("G4 S(\\d+).*");
	
	private float x = 0;
	private float y = 0;
	private float z = 0;
	
	@Property(name = "width")
	private float maxX = 100;
	@Property(name = "depth")
	private float maxY = 100;
	@Property(name = "height")
	private float maxZ = 100;
	
	private float speed = 1;
	
	private float extrusion;
	
	private Logger logger = LoggerFactory.getInstance().getLogger(this.getClass());
		
	private Travel currentTravel;

	private List<TravelListener> travelListeners = new ArrayList<TravelListener>(1);

	@Override
	public void init() {
		super.init();
		y = maxY;
		z = maxZ;
	}
	
	public float getInterpolatedX() {
		if (currentTravel == null) {
			return x;
		}
        long elapsedTime = System.currentTimeMillis() - currentTravel.animationStartTime;
        if (elapsedTime >= currentTravel.durationTimeMillis) {
        	currentTravel = null;
        	return x;
        }
        float t = (float) elapsedTime / currentTravel.durationTimeMillis;
        return lerp(currentTravel.startInterpolationX, x, t);
	}
	
	public float getInterpolatedY() {
	    if (currentTravel == null) {
	        return y;
	    }
	    long elapsedTime = System.currentTimeMillis() - currentTravel.animationStartTime;
	    if (elapsedTime >= currentTravel.durationTimeMillis) {
	        currentTravel = null;
	        return y;
	    }
	    float t = (float) elapsedTime / currentTravel.durationTimeMillis;
	    return lerp(currentTravel.startInterpolationY, y, t);
	}

	public float getInterpolatedZ() {
	    if (currentTravel == null) {
	        return z;
	    }
	    long elapsedTime = System.currentTimeMillis() - currentTravel.animationStartTime;
	    if (elapsedTime >= currentTravel.durationTimeMillis) {
	        currentTravel = null;
	        return z;
	    }
	    float t = (float) elapsedTime / currentTravel.durationTimeMillis;
	    return lerp(currentTravel.startInterpolationZ, z, t);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}
	
	public float getExtrusion() {
		return extrusion;
	}

	public Travel travelTo(String gcode) {
	    if (!gcode.startsWith("G")) {
	        return null;
	    }
	    if (gcode.startsWith("G4 S")) {
			Matcher matcher = G4_PATTERN.matcher(gcode);
			if (matcher.find()) {
				Travel travel = new Travel();
				travel.durationTimeMillis = Integer.parseInt(matcher.group(1));
				return travel;
			}
	    }
	    if (gcode.startsWith("G28")) {
	    	//TODO home travel speed
	    	return travelTo(0,0,0, 1000);
	    }
	    // Extract relevant information from the G-code string
	    float targetX = extractValue(gcode, 'X');
	    float targetY = extractValue(gcode, 'Y');
	    float targetZ = extractValue(gcode, 'Z');
	    float speed = extractValue(gcode, 'F');
	    if (gcode.startsWith("G1")) {
	    	float extrusion = extractValue(gcode, 'E');
	    	return travelTo(targetX, targetY, targetZ, speed, extrusion);	    	
	    } else if (gcode.startsWith("G0")) {
	    	return travelTo(targetX, targetY, targetZ, speed);
	    }
	    return null;
	}

	private float extractValue(String gcode, char axis) {
	    // Extract the value of the specified axis from the G-code string
	    int index = gcode.indexOf(axis);
	    if (index != -1) {
	        try {
	            return Float.parseFloat(gcode.substring(index + 1).split("\\s")[0]);
	        } catch (NumberFormatException e) {
	        	logger.error("could not extract value of " + axis + " axis for gcode " + gcode);
	        }
	    }
	    return Float.NaN; // Default value if axis not found or parsing fails
	}
	
	public Travel travelTo(float targetX, float targetY, float targetZ, float speed) {
		return travelTo(targetX, targetY, targetZ, speed, 0);
	}
	
	public Travel travelTo(float targetX, float targetY, float targetZ, float speed, float extrusion) {
        // Calculate the distance to travel
		float deltaX = Float.isNaN(targetX) ? 0 : targetX - x;
		float deltaY = Float.isNaN(targetY) ? 0 : targetY - y;
		float deltaZ = Float.isNaN(targetZ) ? 0 : targetZ - z;
        this.speed = Float.isNaN(speed) ? this.speed : speed;
        this.extrusion = Float.isNaN(extrusion) ? 0 : extrusion;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        // Calculate the time required to travel
        long timeMillis = (long) ((distance / this.speed) * 60 * 1000); // Assuming speed is in mm/minute

        // Set the values in the Travel object
        Travel travel = new Travel();
        travel.durationTimeMillis = timeMillis;
        travel.distance = distance;
        
        // Update the print head position
        x += deltaX;
        y += deltaY;
        z += deltaZ;

        currentTravel = travel;
        travelListeners.forEach(l -> l.handle(travel));
        return travel;
	}
	
	public void home() {
        x = 0;
        y = 0;
        z = 0;	
    }
	
    private static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
	
	public class Travel {
		
	    private long animationStartTime = System.currentTimeMillis();
	    
	    private float startInterpolationX = getX();
	    
	    private float startInterpolationY = getY();
	    
	    private float startInterpolationZ = getZ();
	    		
		public long durationTimeMillis;
		
		public float distance;
	}

	public void addOnTravelListener(TravelListener travelListener) {
		travelListeners .add(travelListener);
	}
	
}
