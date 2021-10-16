#!/bin/bash

java -cp /home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30014 \
   -downlinkPort 30013 \
   -name ADSB2 \
   -verbose
