package gcode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import app.management.ApplicationService;
import app.management.PropertieService;
import logging.Logger;
import logging.LoggerFactory;

public class MetaDataStorage extends ApplicationService {

	public static final String FLAVOR = "FLAVOR";
	public static final String LAYER = "LAYER";
    public static final String TIME = "TIME";
    public static final String MINX = "MINX";
    public static final String MAXX = "MAXX";
    public static final String MINY = "MINY";
    public static final String MAXY = "MAXY";
    public static final String MINZ = "MINZ";
    public static final String MAXZ = "MAXZ";
    public static final String LAYER_COUNT = "LAYER_COUNT";
    public static final String TYPE = "TYPE";
    public static final String MESH = "MESH";
    public static final String TIME_ELAPSED = "TIME_ELAPSED";    
	/*
	 * TODO
	 * Plugin, generic abstract Class, parameterized Type extends App. 
	 * 
	 * Can add Plugins to app using appmanager
	 * 
	 * predefined trigger?
	 * 
	 * Proxy trigger?
	 * 
	 * 
	 * 
	 */
	
	private Map<String, String> storage = new HashMap<>();
	
	private static final String META_DATA_DEFAULT_REGEX = ";(\\w+):(.+)";
	
	private Pattern metaDataPattern;
	
	private Logger logger = LoggerFactory.getInstance().getLogger(this.getClass());
	
	@Override
	public void init() {
		String metaDataRegex = appManager.getService(PropertieService.class).getProperty("meta-data-pattern", META_DATA_DEFAULT_REGEX);
		try {
			metaDataPattern = Pattern.compile(metaDataRegex);
		} catch (PatternSyntaxException e) {
			logger.error("meta data pattern has incorrect syntax, using predefinded filter", e);
			metaDataPattern = Pattern.compile(META_DATA_DEFAULT_REGEX);
			appManager.getService(PropertieService.class).setProperty("meta-data-pattern", META_DATA_DEFAULT_REGEX);
		}
	}
			
	public void handleLine(String line) {
		Matcher matcher = metaDataPattern.matcher(line);
		if (matcher.matches()) {
			storage.put(matcher.group(1).toUpperCase(), matcher.group(2));
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return null if not present
	 */
	public String getMetaData(String key) {
		return storage.get(key.toUpperCase());
	}

	public void clear() {
		storage.clear();
	}
	
}
