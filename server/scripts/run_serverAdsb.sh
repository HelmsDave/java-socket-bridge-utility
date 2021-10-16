#!/bin/bash

java -cp /home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30004 \
   -downlinkPort 30003 \
   -name ADSB \
   -verbose
