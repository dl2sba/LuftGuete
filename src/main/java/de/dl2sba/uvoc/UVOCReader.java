package de.dl2sba.uvoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

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

		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier aPortId = portList.nextElement();
			if ((aPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)) {
				logger.info("serial port [{}] found", aPortId.getName());
				if (aPortId.getName().equals(portname)) {
					logger.info("desired port named [{}] found", portname);
					rc = aPortId;
					break;
				}
			}
		}
		logger.traceExit();
		return rc;
	}

	public UVOCReader() throws ProcessingException {
		logger.traceEntry();
		setupComPort();
		setupSensor();
		logger.traceExit();
	}

	private void setupSensor() throws ProcessingException {
		logger.traceEntry();
		try {
			this.port.getOutputStream().write(props.getProperty("UVOC.json", "j").getBytes());
			Thread.sleep(1000);
			this.port.getOutputStream().write(props.getProperty("UVOC.datarate", "4").getBytes());
			Thread.sleep(1000);
		} catch (InterruptedException | IOException e) {
			logger.catching(e);
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

	protected void setupComPort() throws ProcessingException {
		logger.traceEntry();
		String comPortName = props.getProperty("UVOC.ComPort", "COM5");
		logger.info("searching for port [{}]", comPortName);
		try {
			CommPortIdentifier portId = getPortIDForName(comPortName);
			if (portId != null) {
				this.port = (SerialPort) portId.open("UVOC", 1);
				logger.info("port opened");
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
			logger.catching(e);
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

	/**
	 * 
	 */
	protected void processLine() {
		logger.traceEntry();
		try {
			String line = this.serialBuffer.toString();
			logger.info("response [{}]", line);
			UVOCSensorData readSensorData = new Gson().fromJson(line, UVOCSensorData.class);
			if (readSensorData != null) {
				new SensorDataPublisher().publish(readSensorData);
			}
		} catch (JsonSyntaxException e) {
			logger.info("failed to parse JSON info");
		} catch (MqttException e) {
			logger.catching(e);
		}
		this.serialBuffer = new StringBuilder();
		logger.traceExit();
	}

	/**
	 * Read data from the open serial port.
	 * 
	 * Maximum of 1000 chars are read, rest is discarded.
	 * 
	 * Append data to the end of a buffer until CR or LF is received. Then
	 * process the line.
	 * 
	 * @param endWithLF
	 */
	void readSerialChar(boolean endWithLF) {
		logger.traceEntry();
		try {
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
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.traceExit();
	}

	/**
	 * 
	 */
	public void run() {
		logger.traceEntry();
		readSerialChar(true);
		logger.traceExit();
	}

}
