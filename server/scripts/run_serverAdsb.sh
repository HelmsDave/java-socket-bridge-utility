#!/bin/bash

exec java -cp "/home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar:/home/ec2-user/java-socket-bridge-utility/server/target/deps/*" \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30004 \
   -downlinkPort 30003 \
   -name ADSB_Columbia \
   -verbose -archive 2>&1 | tee -a /tmp/serverAdsb.log
