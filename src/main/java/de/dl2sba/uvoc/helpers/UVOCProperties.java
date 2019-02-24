package de.dl2sba.uvoc.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UVOCProperties extends TypedProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 654654602341798406L;
	private static UVOCProperties singleton = null;

	protected static final Logger logger = LogManager.getLogger(UVOCProperties.class.getName());

	private void load(String propertiesFileName, Properties defaultProperties) {
		logger.traceEntry("propertiesFileName={}", propertiesFileName);

		InputStream is;
		try {

			is = new FileInputStream(propertiesFileName);
			load(is);
			is.close();
			logger.info("Loaded properties from [{}].", propertiesFileName);
		} catch (FileNotFoundException e) {
			putAll(defaultProperties);
			logger.info("File [{}] not found, using default.", propertiesFileName);
		} catch (IOException e) {
			logger.info("File [{}] not readable, using default.", propertiesFileName);
		}

		logger.traceExit();
	}

	/**
	 * Return the one and only instance of the config object
	 * 
	 * @return the only instance of this class
	 */
	public static synchronized UVOCProperties getSingleton() {
		if (singleton == null) {
			singleton = new UVOCProperties();
			// check if environment var is set
			String propertyFile = System.getProperty("UVOC.properties");

			/// set?
			if (propertyFile == null) {
				// no use default in code-tree
				propertyFile = "./UVOC.properties";
			}
			logger.debug("Loading properties from [{}]", propertyFile);

			// load properties
			singleton.load(propertyFile, new Properties());
		}
		return singleton;
	}
}
