#!/bin/bash

java -cp /home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30009 \
   -downlinkPort 30008 \
   -name AIS \
   -verbose
   