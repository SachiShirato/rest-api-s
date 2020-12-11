#!/bin/bash
set -eux
base=$(dirname $(readlink -f $0))

[ $(whoami) = root ]

cd /vagrant/mq
sudo ./install.sh

cd /vagrant/mb
sudo ln -s /lib64/libc.so.6 /lib/libc.so.6
yes | sudo yum install ld-linux.so.2
yes | sudo yum install libgcc_s.so.1

sudo ./install.sh


cd /vagrant/mb/fp
sudo ./install.sh 9

sudo mkdir -p /acom/it/logs/mid/wmb
sudo mkdir -p /acom/it/config/mid/wmb
sudo mkdir -p /var/mqsi/converters/icudt51l

sudo chown mqm:mqm -R /var/mqsi/converters
sudo chown mqm:mqm -R /acom
sudo chmod 755 -R /acom
sudo chmod 755 -R /var/mqsi/converters/

sudo cp /vagrant/mb/ibm-943_P130-1999.cnv /var/mqsi/converters/icudt51l/
sudo chmod 644 /var/mqsi/converters/icudt51l/ibm-943_P130-1999.cnv
sudo cp /vagrant/mb/RizaConfig.js /acom/it/config/mid/wmb/

sudo chown mqm:mqm -R /acom/
sudo chmod 644 /acom/it/config/mid/wmb/RizaConfig.js

cd /vagrant/
sudo -u mqm ./create.sh

