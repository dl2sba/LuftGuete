# uVOC
Java client to pull sensor data from an attached uThing::VOC sensor and publish it to an MQTT broker

## Basic function
Every time the uVOC sends sensor data it is published to the MQTT broker.

## Polling
This code is expected to run forever. The main loop is defined like this:

## Running
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

Launch the JAR with **java -jar uVOC-1.5-jar-with-dependencies.jar**

## credits
This work is based on the published information from Ohmtech.io  / uThing::VOC
See also https://github.com/ohmtech-io/uThingVOC
