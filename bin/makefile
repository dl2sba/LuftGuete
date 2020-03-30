#
#       (c) Dietmar Krause 2020
#
#       Raspberry Pi 2 Model B Makefile for UVOCReader
#
#       30.03.2020
#
BIN     = UVOC
INSTDIR = /usr/local/bin
INITD   = /etc/init.d

default:	help


run:
	/usr/bin/java -Dlog4j.configurationFile=UVOC.log4j2.xml -DUVOC.properties=UVOC.properties -jar uVOC-1.5-jar-with-dependencies.jar

install:
	sudo cp uVOC-1.5-jar-with-dependencies.jar $(INSTDIR)
	sudo cp runUVOC.sh	$(INSTDIR)
	sudo cp UVOC.log4j2.xml $(INSTDIR)
	sudo cp UVOC.properties $(INSTDIR)
	sudo cp UVOC.sh 	$(INITD)
	sudo update-rc.d 	$(BIN).sh defaults

uninstall:
	sudo update-rc.d $(BIN).sh remove
	sudo rm $(INSTDIR)/uVOC-1.5-jar-with-dependencies.jar 
	sudo rm $(INSTDIR)/runUVOC.sh
	sudo rm $(INSTDIR)/UVOC.log4j2.xml
	sudo rm $(INSTDIR)/UVOC.properties
	sudo rm $(INITD)/UVOC.sh

start:
	sudo /etc/init.d/$(BIN).sh start

stop:
	sudo /etc/init.d/$(BIN).sh stop

help:
	@echo "\n"
	@echo "Supported commands"
	@echo "------------------"
	@echo "  install - copy all files to $(INSTDIR)"
	@echo "  run     - compile all and run executable as root"
	@echo "\n"
	@echo "start daemon with"
	@echo "   sudo /etc/init.d/$(BIN).sh start"
	@echo "\n"
	@echo "stop daemon with"
	@echo "   sudo /etc/init.d/$(BIN).sh stop"
	@echo "\n"
	@echo "Links for init.d are created here"
	@echo "   "`ls /etc/rc?.d/*$(BIN).sh`
	@echo "\n"
	@echo "PID file created here"
	@echo "   /run/$(BIN)/$(BIN).pid"
	@echo "   current PID "`cat /run/$(BIN)/$(BIN).pid`
	@echo "\n"                                    
