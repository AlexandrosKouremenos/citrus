#!/bin/bash

if [ -z "$1" ]; then
  echo "Too few arguments."
  exit
fi

service_name="aurantium"
replica_num=$1

# Loop and start the replicas
for i in $(seq 0 $((replica_num - 1))); do

  echo "Starting ${service_name}_${i}."
  docker compose run -d --name "${service_name}_${i}" --env "machine.id=${i}" "$service_name"
  sleep 5

done
