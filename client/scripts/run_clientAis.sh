#!/bin/bash

java -cp /home/pi/java-socket-bridge-utility/client/target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.bridge.SockClientRelayMain \
   -pullHost localhost -pullPort 30008 \
   -pushHost 23.21.112.235 -pushPort 30009 \
   -verbose

