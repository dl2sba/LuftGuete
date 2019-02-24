package de.dl2sba.uvoc;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.dl2sba.uvoc.data.UVOCSensorData;
import de.dl2sba.uvoc.helpers.ProcessingException;
import de.dl2sba.uvoc.helpers.UVOCProperties;
import de.dl2sba.uvoc.publisher.SensorDataPublisher;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class UVOCReader {
	private static final int MAX_BUFFER_LEN = 1000;
	protected static final Logger logger = LogManager.getLogger(UVOCReader.class.getName());
	protected static final UVOCProperties props = UVOCProperties.getSingleton();

	protected SerialPort port = null;
	protected StringBuilder serialBuffer = new StringBuilder();

	protected CommPortIdentifier getPortIDForName(String portname) {
		logger.traceEntry(portname);
		CommPortIdentifier rc = null;

		var portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier aPortId = portList.nextElement();
			if ((aPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)) {
				logger.debug("serial port [{}] found", aPortId.getName());
				if (aPortId.getName().equals(portname)) {
					logger.debug("desired port named [{}] found", portname);
					rc = aPortId;
					break;
				}
			}
		}
		logger.traceExit();
		return rc;
	}

	/**
	 * 
	 * @throws ProcessingException
	 */
	public UVOCReader() {
		logger.traceEntry();
		logger.traceExit();
	}

	/**
	 * read and discard all serial character currently available at serial port
	 */
	public void flushInput() {
		logger.traceEntry();
		if (this.port != null) {
			InputStream input;
			try {
				input = this.port.getInputStream();
				int numChars = input.available();
				if (numChars > 0) {
					input.readNBytes(numChars);
					logger.debug("{} bytes skipped on input stream", numChars);
				}
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		logger.traceExit();
	}

	/**
	 * Setup the sensor.
	 * 
	 * Means, set the data mode to JSON via [UVOC.json] and set interval to
	 * [UVOC.datarate]
	 * 
	 * @throws ProcessingException
	 */
	private void setupSensor() throws ProcessingException {
		logger.traceEntry();
		try {
			this.port.getOutputStream().write(props.getProperty("UVOC.json", "j").getBytes());
			logger.info("sensor switched to JSON format");
			Thread.sleep(1000);

			this.port.getOutputStream().write(props.getProperty("UVOC.datarate", "4").getBytes());
			logger.info("sensor switched desired data rate");
			Thread.sleep(1000);
		} catch (InterruptedException | IOException e) {
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

	/**
	 * search for the defined serial port and try to open it
	 * 
	 * @throws ProcessingException
	 *             When port not found or open failed
	 */
	protected void setupComPort() throws ProcessingException {
		logger.traceEntry();
		String comPortName = props.getProperty("UVOC.ComPort", "COM5");
		logger.debug("searching for port [{}]", comPortName);
		try {
			var portId = getPortIDForName(comPortName);
			if (portId != null) {
				this.port = (SerialPort) portId.open("UVOC", 1);
				logger.debug("port opened");
				this.port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				this.port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				this.port.enableReceiveTimeout(1000);
				this.port.setInputBufferSize(100000);
				logger.info("port setup done");

			} else {
				logger.error("port {} not found", comPortName);
				throw new ProcessingException("serial port not found");
			}
		} catch (PortInUseException | UnsupportedCommOperationException e) {
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

	/**
	 * 
	 */
	protected void processLine() {
		logger.traceEntry();
		String line = null;
		try {
			line = this.serialBuffer.toString();
			logger.debug("response [{}]", line);
			UVOCSensorData readSensorData = new Gson().fromJson(line, UVOCSensorData.class);
			if (readSensorData != null) {
				new SensorDataPublisher().publish(readSensorData);
			}

			flushInput();
		} catch (JsonSyntaxException e) {
			logger.warn("failed to parse JSON message [{}]", line);
		} catch (MqttException e) {
			logger.catching(e);
		}
		this.serialBuffer = new StringBuilder();
		logger.traceExit();
	}

	/**
	 * If the port ist currently not set, try to open the port. Then read data
	 * from the open serial port.
	 * 
	 * Maximum of 1000 chars are read, rest is discarded.
	 * 
	 * Append data to the end of a buffer until CR or LF is received. Then
	 * process the line.
	 * 
	 * @param endWithLF
	 * @throws ProcessingException
	 */
	public void run(boolean endWithLF) throws ProcessingException {
		logger.traceEntry();
		try {
			if (this.port == null) {
				setupComPort();
				setupSensor();
				flushInput();
			}
			InputStream stream = this.port.getInputStream();
			while (stream.available() > 0) {
				int ch = stream.read();
				if (ch == 13) {
					if (!endWithLF) {
						processLine();
					}
				} else if (ch == 10) {
					if (endWithLF) {
						processLine();
					}
				} else {
					if (this.serialBuffer.length() < MAX_BUFFER_LEN) {
						this.serialBuffer.append((char) ch);
					}
				}
			}
		} catch (ProcessingException e) {
			if (this.port != null) {
				this.port.close();
				this.port = null;
			}
			throw e;
		} catch (IOException e) {
			this.port.close();
			this.port = null;
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

}
