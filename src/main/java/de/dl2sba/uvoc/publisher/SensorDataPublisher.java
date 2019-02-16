package de.dl2sba.uvoc.publisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.dl2sba.uvoc.data.UVOCSensorData;
import de.dl2sba.uvoc.helpers.UVOCProperties;

public class SensorDataPublisher implements MqttCallback {

	protected static final Logger logger = LogManager.getLogger(SensorDataPublisher.class.getName());
	protected static final UVOCProperties props = UVOCProperties.getSingleton();

	private final MemoryPersistence mqttPersistence = new MemoryPersistence();
	private MqttClient mqttClient = null;

	/**
	 * 
	 * @param nodeId
	 * @param sensorId
	 * @param value
	 * @throws MqttException
	 */
	private void publishOneSensor(int nodeId, int sensorId, double value) throws MqttException {
		logger.traceEntry();

		String topicPattern = SensorDataPublisher.props.getProperty("MQTT.Topic");

		int qos = SensorDataPublisher.props.getInteger("MQTT.qos", 1);

		String topic = String.format(topicPattern, nodeId, sensorId);
		String val = Double.toString(value);

		this.mqttClient.publish(topic, val.getBytes(), qos, false);
		logger.info("Published {} to {}/{}", value, nodeId, sensorId);
		logger.traceExit();
	}

	/**
	 * 
	 * @param data
	 * @param nodeId
	 * @param sensorId
	 * @throws MqttException
	 */
	public void publish(UVOCSensorData data) throws MqttException {
		logger.traceEntry();
		String clientId = MqttClient.generateClientId();

		this.mqttClient = new MqttClient(props.getProperty("MQTT.Server"), clientId, mqttPersistence);
		this.mqttClient.setCallback(this);

		int nodeId = props.getInteger("MQTT.nodeId", 1234567);

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setPassword(props.getProperty("MQTT.Password").toCharArray());
		connOpts.setUserName(props.getProperty("MQTT.User"));
		this.mqttClient.connect(connOpts);

		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.temp.id", 0), data.getTemperature());
		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.humidity.id", 1), data.getHumidity());
		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.pressure.id", 2), data.getPressure());
		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.gasResistance.id", 3), data.getGasResistance());
		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.IAQ.id", 4), data.getIAQ());
		publishOneSensor(nodeId, props.getInteger("MQTT.sensor.iaqAccuracy.id", 5), data.getIaqAccuracy());

		mqttClient.disconnect();
		logger.traceExit();
	}

	@Override
	public void connectionLost(Throwable arg0) {
		logger.traceEntry();
		logger.traceExit();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		logger.traceEntry();
		logger.traceExit();
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		logger.traceEntry();
		logger.traceExit();
	}

}
