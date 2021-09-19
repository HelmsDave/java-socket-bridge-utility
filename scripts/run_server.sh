#!/bin/bash

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30004 \
   -downlinkPort 30003 \
   -verbose
