#!/bin/bash

cd pomelo/cluster-setup || exit
bash pomelo-setup.sh

#sleep 60
#
#cd pomelo/cluster-setup/metrics/prometheus || exit
#bash prometheus-setup.sh
#
#sleep 60
#
#cd pomelo/cluster-setup/metrics/grafana || exit
#bash grafana-setup.sh

sleep 60

cd sunki/cluster-setup || exit
bash sunki-setup.sh

sleep 60

cd  aurantium/scripts || exit
bash aurantium/scripts/start-aurantium.sh 5