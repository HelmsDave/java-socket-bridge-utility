#!/bin/bash

java -cp /home/pi/java-socket-bridge-utility/target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.bridge.SockClientRelayMain \
   -pullHost localhost -pullPort 30008 \
   -pushHost 3.84.218.184 -pushPort 30009 \
   -verbose
