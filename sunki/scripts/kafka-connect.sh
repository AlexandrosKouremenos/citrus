#!/bin/bash

cd ~/Utilities/kafka-3.4.0-src/ || exit

bin/connect-standalone.sh config/sunki/connect-standalone.properties config/sunki/connect-mqtt-source.properties
