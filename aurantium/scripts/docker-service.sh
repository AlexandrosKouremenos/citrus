#!/bin/bash

if [ -z "$1" ]; then
  echo "Too few arguments."
  exit
fi

service_name="aurantium"

# Number of replicas.
replicas=$1

# Start sunki service.
docker compose up sunki -d

# Loop and start the replicas
for i in $(seq 0 $((replicas - 1)) ); do

  echo "Starting ${service_name}_${i}."
  docker compose run -d --name "${service_name}_${i}" --env "machine.id=${i}" "$service_name"

done
