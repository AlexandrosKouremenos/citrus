#!/bin/bash

project_dir=$(pwd | grep -o '.*citrus')

if [ -z "${project_dir}" ]; then
    echo "Citrus directory not found."
    exit 1
fi

source "${project_dir}"/citrus-paths.sh

# create registry container unless it already exists
reg_name='citrus-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --name "${reg_name}" \
    registry:2
fi

cd "${SUNKI_PATH}" || exit

# TODO: We need the persistent .yaml in case of failure.
kubectl --context=kind-sunki-cluster apply -f src/test/strimzi/kafka-ephemeral-single.yaml

echo "*** Waiting for Strimzi Entity Operator to complete its setup. ***"
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl --context=kind-sunki-cluster wait --namespace default \
                --for=condition=Available \
                deployment/sunki-cluster-entity-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "*** Strimzi Entity Operator setup complete. ***"
    fi

done