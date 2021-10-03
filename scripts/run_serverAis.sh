#!/bin/bash

java -cp /home/ec2-user/java-socket-bridge-utility/target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30009 \
   -downlinkPort 30008 \
   -verbose
   