#!/bin/bash

# Global environment.
# Application product name
APPLICATION_PRODUCT="mapserver"

# Application home directory
APPLICATION_HOME="/opt/tracker"

# PID for admin server
APPLICATION_PID="/var/run/mapserver-tracker.pid"

# Name
APPLICATION_NAME="tracker"

# Java command
JAVA="/usr/bin/java"

JAVA_OPTS="-server -Xms128m -Xmx1536m"

# JPDA options to enable remote debugging.
# JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Setup program specific properties
JAVA_OPTS="$JAVA_OPTS -Dapplication.name=$APPLICATION_NAME"
JAVA_OPTS="$JAVA_OPTS -Dapplication.home=$APPLICATION_HOME"

# Setup the library path
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$APPLICATION_HOME/lib"

# Headless mode for AWT
JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"

# Setup the classpath
APPLICATION_CLASSPATH="$APPLICATION_CLASSPATH:$APPLICATION_HOME/lib/*"

# for file attributes
umask 022
ulimit -n 8096

# By default it's all good.
RETVAL=0

# Start or stop
case "$1" in
    start)
        shift
        exec -a "$APPLICATION_PRODUCT-$APPLICATION_NAME" "$JAVA" $JAVA_OPTS -XX:OnOutOfMemoryError="kill -9 %p" -Dapplication.classpath="$APPLICATION_CLASSPATH" -classpath "$APPLICATION_CLASSPATH" Main $APPLICATION_NAME "$@" &
        RETVAL=$?
        if [ ! -z "$APPLICATION_PID" ]; then
            echo $! > $APPLICATION_PID
        fi
        ;;
    stop)
        shift
        if [ ! -z "$APPLICATION_PID" ] && [ -e "$APPLICATION_PID" ] && [ -e /proc/`cat $APPLICATION_PID` ]; then
            kill -TERM `cat $APPLICATION_PID`
            RETVAL=$?
            rm -rf $APPLICATION_PID
        fi
        ;;
    restart)
        $0 stop
        sleep 3
        $0 start
        RETVAL=$?
        ;;
    run)
        shift
        "$JAVA" $JAVA_OPTS -classpath "$APPLICATION_CLASSPATH" Main $APPLICATION_NAME "$@"
        RETVAL=$?
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|run}"
        exit 1
esac

exit ${RETVAL};