package gcode;

import gcode.PrintHead.Travel;
import logging.Logger;
import logging.LoggerFactory;

public class GCodeResponseTimeRetardend {
		
	private static Logger logger = LoggerFactory.getInstance().getLogger(GCodeResponseTimeRetardend.class);
	
	public static void waitFor(Travel travel) {
		if (travel == null) {
			return;
		}
		long waitTimeMillis = Math.max(travel.durationTimeMillis - 100, 0);
		if (waitTimeMillis == 0) {
			return;
		}
		try {
			logger.debug("retarding " + waitTimeMillis + "ms");
			Thread.sleep(waitTimeMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
