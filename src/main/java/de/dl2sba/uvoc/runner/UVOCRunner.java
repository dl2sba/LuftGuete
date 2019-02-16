package de.dl2sba.uvoc.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.uvoc.UVOCReader;
import de.dl2sba.uvoc.helpers.ProcessingException;
import de.dl2sba.uvoc.helpers.UVOCProperties;

public class UVOCRunner {

	protected static final Logger logger = LogManager.getLogger(UVOCRunner.class.getName());
	protected static final UVOCProperties props = UVOCProperties.getSingleton();
	protected boolean endAll = false;

	public static void main(String[] args) {
		logger.traceEntry();
		new UVOCRunner();
		logger.traceExit();
		System.exit(0);
	}

	public UVOCRunner() {
		logger.traceEntry();
		logger.info("***************************************************************");
		logger.info("Application information:");
		Attributes attributes = getAttributesFromManifest();
		Set<Entry<Object, Object>> entries = attributes.entrySet();
		Iterator<Entry<Object, Object>> it = entries.iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> pair = it.next();
			logger.info("   {} : {}", pair.getKey(), pair.getValue());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook ran!");
				endAll = true;
			}
		});

		// time between wakeups in s
		long sleepTime = props.getLong("Runner.sleep", 1);
		logger.info("Sleep between time checks....{}s", sleepTime);

		sleepTime *= 1000;

		UVOCReader instance;
		try {
			instance = new UVOCReader();
			while (!endAll) {
				// sleep one sec
				instance.run();
				Thread.sleep(sleepTime);
			}
		} catch (ProcessingException | InterruptedException e1) {
			logger.catching(e1);
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
