#!/bin/bash
set -eux
base=$(dirname $(readlink -f $0))

[ $(whoami) = root ]

VARMC=/var/mqsi/converters
VAGM=/vagrant/m
ACOMI=/acom/it/config/mid/wmb


${VAGM}q/install.sh

ln -s /lib64/libc.so.6 /lib/libc.so.6
yum -y install ld-linux.so.2
yum -y install libgcc_s.so.1
${VAGM}b/install.sh

${VAGM}b/fp/install.sh 9

mkdir -p /acom/it/{logs,config}/mid/wmb
mkdir -p ${VARMC}/icudt51l

cp ${VAGM}b/ibm-943_P130-1999.cnv ${VARMC}/icudt51l/
cp ${VAGM}b/RizaConfig.js ${ACOMI}

chown mqm:mqm -R ${VARMC}
chown mqm:mqm -R /acom

find ${VARMC} -type d -print0 | xargs -0 chmod 755
find ${VARMC} -type f -print0 | xargs -0 chmod 644

find /acom -type d -print0 | xargs -0 chmod 755
find /acom -type f -print0 | xargs -0 chmod 644

sudo -u mqm /vagrant/create.sh

