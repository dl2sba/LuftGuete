
# uVOC
Java client to pull sensor data from an attached uThing::VOC sensor and publish it to an MQTT broker

## Basic function
Every time the uVOC sends sensor data it is published to the MQTT broker.

The truth is in the code ;-)

## Polling
This code is expected to run forever. The main loop is defined like this:

## Running from commandline
Download the JAR with the dependencies.
Create a file named **UVOC.properties** near to the JAR with the following content:
```
MQTT.Password=<<password>>
MQTT.Server=tcp\://<<IP>>\:<<port>>
MQTT.Topic=dl2sba.de/sensorData/%d/%d
MQTT.User=dl2sba
MQTT.nodeId=9800
MQTT.qos=1
MQTT.sensor.IAQ.id=5
MQTT.sensor.gasResistance.id=4
MQTT.sensor.humidity.id=1
MQTT.sensor.iaqAccuracy.id=6
MQTT.sensor.pressure.id=3
MQTT.sensor.temp.id=0
Runner.endWithLF=true
Runner.sleep=1
UVOC.ComPort=<<the serial port>>
UVOC.datarate=4
UVOC.json=j
```
Create a file named **UVOC.log4j2.xml** near to the JAR with the following content:
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<Configuration status="info" monitorInterval="30">
	<Appenders>
		<Console name="Console" target="SYSTEM_OUT">
			<PatternLayout
				pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{1} %M - %msg%n" />
		</Console>
	</Appenders>
	<Loggers>
		<Root level="info">
			<AppenderRef ref="Console" />
		</Root>
	</Loggers>
</Configuration>
```
Launch the JAR with **java -Dlog4j.configurationFile=UVOC.log4j2.xml -DUVOC.properties=UVOC.properties-jar uVOC-1.6-jar-with-dependencies.jar**


## Running using systemd
Tested on an Raspberry Pi 3 with "Linux UVOC 4.19.97-v7+ #1294 SMP Thu Jan 30 13:15:58 GMT 2020 armv7l GNU/Linux" based on this [post](https://stackoverflow.com/questions/21503883/spring-boot-application-as-a-service/22121547#22121547).

Copy the following files to the desired *[location]*:
 - UVOC.properties
 - UVOC.log4j2.xml
 - uVOC-1.6-jar-with-dependencies.jar

Easiest way is to copy them into the users home directory.

Create file **/etc/systemd/UVOC.service** with the following content:
```
[Unit]
Description=UVOC Service

[Service]
#  User must be member of dialout group to have access to serial port
User=[desired user to run as]

# The configuration file application.properties should be here:
WorkingDirectory=[location]
ExecStart=/usr/bin/java -Dlog4j.configurationFile=UVOC.log4j2.xml -DUVOC.properties=UVOC.properties -jar uVOC-1.6-jar-with-dependencies.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=30

[Install]
WantedBy=multi-user.target
```
After creating this file reload the systemd config with **sudo systemctl daemon-reload**.

Starting the daemon manually **sudo systemctl start UVOC**.

Stopping the daemon manually **sudo systemctl stop UVOC**.

Query the status of the daemon  **sudo systemctl status UVOC**.


## credits
This work is based on the published information from Ohmtech.io  / uThing::VOC
See also https://github.com/ohmtech-io/uThingVOC
