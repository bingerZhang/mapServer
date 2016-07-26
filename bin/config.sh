#!/bin/bash

# Global environment.
# Application home directory
APPLICATION_HOME="/opt/tracker"

APPLICATION_NAME="config"

# Java command
JAVA="/usr/bin/java"

# Sun JVM memory allocation pool parameters.
JAVA_OPTS="-server -Xms32m -Xmx1g"

# Setup program specific properties
JAVA_OPTS="$JAVA_OPTS -Dapplication.name=$APPLICATION_NAME"
JAVA_OPTS="$JAVA_OPTS -Dapplication.home=$APPLICATION_HOME"

# Setup the library path
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$APPLICATION_HOME/lib"

# Setup the classpath
APPLICATION_CLASSPATH="$APPLICATION_HOME/lib/*"

# Run config tools
"$JAVA" $JAVA_OPTS -classpath "$APPLICATION_CLASSPATH" Main $APPLICATION_NAME "$@"
RETVAL=$?

exit $RETVAL
