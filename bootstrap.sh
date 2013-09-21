#!/usr/bin/env bash

set -x
export DEBIAN_FRONTEND=noninteractive

if [ ! -e "/home/vagrant/.firstboot" ]; then
  dpkg --purge ufw
  apt-get update
  apt-get install -y --force-yes vim curl unzip software-properties-common python-software-properties
  add-apt-repository ppa:webupd8team/java
  apt-get update
  echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
  apt-get install -y --force-yes oracle-java7-installer

  # install play 2.1.3
  cd /opt
  wget http://downloads.typesafe.com/play/2.1.3/play-2.1.3.zip 
  unzip play-2.1.3.zip
  ln -s /opt/play-2.1.3/play /usr/bin/play  
  chown -R vagrant:vagrant /opt/play-2.1.3

  curl -L cloudbees-downloads.s3.amazonaws.com/sdk/cloudbees-sdk-1.5.0-bin.zip > bees_sdk.zip
  unzip bees_sdk.zip
  chown -R vagrant:vagrant /opt/cloudbees-sdk-1.5.0
  mkdir /home/vagrant/.bees
  mv /vagrant/bees.config.template /home/vagrant/.bees/bees.config  

  mv /etc/localtime /etc/localtime.bak
  ln -s /usr/share/zoneinfo/Asia/Manila /etc/localtime

  touch /home/vagrant/.firstboot
  reboot
fi

export BEES_HOME=/opt/cloudbees-sdk-1.5.0
export PATH=$PATH:$BEES_HOME

