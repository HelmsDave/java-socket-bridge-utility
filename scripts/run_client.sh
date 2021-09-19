#!/bin/bash

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.bridge.SockClientRelayMain \
   --pullHost localhost --pullSock 30003 \
   --pushHost localhost --pushSock 30004 \
   --verbose
