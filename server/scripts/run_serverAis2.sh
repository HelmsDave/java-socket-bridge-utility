#!/bin/bash

java -cp /home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30019 \
   -downlinkPort 30018 \
   -name AIS_Columbia \
   -verbose -archive
   