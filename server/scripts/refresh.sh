#!/bin/sh

cd ~
sudo systemctl stop serverSocketAdsb
sudo systemctl stop serverSocketAdsb2
sudo systemctl stop serverSocketAis
sudo systemctl stop serverSocketAis2

rm java-socket-bridge-utility -rf

git clone https://github.com/HelmsDave/java-socket-bridge-utility.git

chmod a+x ~/java-socket-bridge-utility/server/scripts/*.sh

cd ~/java-socket-bridge-utility/server
mvn clean install

sudo systemctl start serverSocketAdsb
sudo systemctl start serverSocketAdsb2
sudo systemctl start serverSocketAis
sudo systemctl start serverSocketAis2

sudo systemctl status serverSocketAdsb
sudo systemctl status serverSocketAdsb2
sudo systemctl status serverSocketAis
sudo systemctl status serverSocketAis2
