#!/bin/bash

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30004 \
   -downlinkPort 30003 \
   -verbose > /tmp/Server_30003.log &

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30009 \
   -downlinkPort 30008 \
   -verbose > /tmp/Server_30008.log &
   
java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30014 \
   -downlinkPort 30013 \
   -verbose > /tmp/Server_30013.log &

java -cp ../target/java-socket-bridge-utility-1.0-SNAPSHOT.jar \
   org.harmonograph.socket.server.SockServerRelayMain \
   -uplinkPort 30019 \
   -downlinkPort 30018 \
   -verbose > /tmp/Server_30018.log &   