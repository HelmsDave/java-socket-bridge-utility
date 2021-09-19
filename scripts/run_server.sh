#!/bin/bash

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   --pullHost localhost --pullSock 30004 \
   --pushHost localhost --pushSock 30003 \
   --verbose
