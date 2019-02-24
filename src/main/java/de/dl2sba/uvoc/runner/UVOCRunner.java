package de.dl2sba.uvoc.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.uvoc.UVOCReader;
import de.dl2sba.uvoc.helpers.ProcessingException;
import de.dl2sba.uvoc.helpers.UVOCProperties;

/**
 * 
 * @author dietmar
 *
 */
public class UVOCRunner {

	protected static final Logger logger = LogManager.getLogger(UVOCRunner.class.getName());
	protected static final UVOCProperties props = UVOCProperties.getSingleton();
	protected boolean endAll = false;

	/**
	 * Main loop.
	 * 
	 * Create an instance and see what happens
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		new UVOCRunner();
		logger.traceExit();
		System.exit(0);
	}

	/**
	 * Dump application information data from the manifest. Register the system
	 * shutdown hook.
	 * 
	 * Then go into endless loop.
	 */
	public UVOCRunner() {
		logger.traceEntry();
		logger.info("***************************************************************");
		logger.info("Application information:");
		Attributes attributes = getAttributesFromManifest();
		var entries = attributes.entrySet();
		var it = entries.iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> pair = it.next();
			logger.info("   {} : {}", pair.getKey(), pair.getValue());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.warn("Shutdown hook ran!");
				endAll = true;
			}
		});

		// time between wakeups in s
		long sleepTime = props.getLong("Runner.sleep", 1);
		logger.info("Sleep between time checks....{}s", sleepTime);

		sleepTime *= 1000;

		UVOCReader instance;
		instance = new UVOCReader();
		while (!endAll) {
			try {
				// sleep one sec
				instance.run(props.getBoolean("Runner.endWithLF", true));
				Thread.sleep(sleepTime);
			} catch (InterruptedException | ProcessingException e) {
				logger.catching(e);
				try {
					Thread.sleep(10 * sleepTime);
				} catch (InterruptedException e1) {
					logger.catching(e);
				}
			}
		}
		logger.info("Main instance ended");
		logger.traceExit();
	}

	/**
	 * 
	 * @return
	 */
	Attributes getAttributesFromManifest() {
		Attributes rc = null;
		InputStream manifestStream = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
		if (manifestStream != null) {
			Manifest manifest;
			try {
				manifest = new Manifest(manifestStream);
				rc = manifest.getMainAttributes();
			} catch (IOException e) {
				logger.catching(e);
				rc = new Attributes();
			}
		} else {
			rc = new Attributes();
		}
		return rc;
	}

}
