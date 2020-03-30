#!/bin/bash

### BEGIN INIT INFO
# Provides:          UVOC
# Required-Start:    $network $remote_fs $syslog $time
# Required-Stop:     $network $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: uVOC Sensor
# Description:       Read the uVOC sensor and send to MQTT broker
### END INIT INFO

. /lib/lsb/init-functions

DAEMON=/usr/local/bin/runUVOC.sh
RUNDIR=/run/UVOC
PIDFILE=$RUNDIR/UVOC.pid
LOGFILE=/var/log/UVOC.log
RUNAS=root

test -x $DAEMON || exit 5

start() {
    log_daemon_msg "Starting UVOC server" "UVOC"
    log_daemon_msg "daemon $DAEMON" "UVOC"

    # ensure the logfile exists with the right ownership
    touch $LOGFILE
    chown $RUNAS $LOGFILE
    chmod u+rw $LOGFILE

    # create $RUNDIR for $STATUSFILE, $PIDFILE
    mkdir -p $RUNDIR
    chown $RUNAS $RUNDIR
    chmod 0777 $RUNDIR

    /sbin/start-stop-daemon --start --oknodo --background --user $RUNAS --pidfile $PIDFILE  --make-pidfile --chuid $RUNAS --exec $DAEMON
    status=$?
    log_end_msg $status
    return
}

stop() {
    log_daemon_msg "Stopping UVOC server" "UVOC"
    /sbin/start-stop-daemon --stop --quiet --oknodo --user $RUNAS --pidfile $PIDFILE --exec $DAEMON --retry 5
    log_end_msg $?
    rm -f $PIDFILE
    return
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    *)
		echo "Usage: $0 {start|stop}"
		exit 2
		;;
esac

