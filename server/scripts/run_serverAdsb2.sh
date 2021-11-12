#!/bin/bash

exec java -cp "/home/ec2-user/java-socket-bridge-utility/server/target/java-socket-server-utility-1.0-SNAPSHOT.jar:/home/ec2-user/java-socket-bridge-utility/server/target/deps/*" \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30014 \
   -downlinkPort 30013 \
   -name ADSB_Dallas \
   -verbose -archive 2>&1 | tee -a /tmp/serverAdsb2.log
