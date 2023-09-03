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

# create a cluster with the local registry enabled in containerd
kind create cluster --config pomelo-cluster.yaml

docker tag pomelo:latest localhost:${reg_port}/pomelo:latest

docker push localhost:${reg_port}/pomelo:latest

# connect the registry to the cluster network if not already connected
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

cd "${STRIMZI_PATH}" || exit

docker pull quay.io/strimzi/operator:0.34.0

docker tag quay.io/strimzi/operator:0.34.0 localhost:${reg_port}/quay.io/strimzi/operator:0.34.0

docker push localhost:${reg_port}/quay.io/strimzi/operator:0.34.0

sed -i "s|image: quay.io/strimzi/operator:0.34.0|image: localhost:5001/quay.io/strimzi/operator:0.34.0|g" install/cluster-operator/*
sed -i "s|value: quay.io/strimzi/operator:0.34.0|value: localhost:5001/quay.io/strimzi/operator:0.34.0|g" install/cluster-operator/*

sed -i 's|namespace: .*|namespace: default|' install/cluster-operator/*RoleBinding*.yaml

kubectl --context=kind-pomelo-cluster create clusterrolebinding strimzi-cluster-operator-namespaced \
        --clusterrole=strimzi-cluster-operator-namespaced \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl --context=kind-pomelo-cluster create clusterrolebinding strimzi-cluster-operator-watched \
        --clusterrole=strimzi-cluster-operator-watched \
        --serviceaccount \
         default:strimzi-cluster-operator

kubectl --context=kind-pomelo-cluster create clusterrolebinding strimzi-cluster-operator-entity-operator-delegation \
        --clusterrole=strimzi-entity-operator \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl --context=kind-pomelo-cluster create -f install/cluster-operator/ -n default

cd "${POMELO_PATH}" || exit

# TODO: We need the persistent .yaml in case of failure.
kubectl --context=kind-pomelo-cluster apply -f cluster-setup/kafka-cluster/kafka-ephemeral.yaml

echo "*** Waiting for Strimzi Entity Operator to complete its setup. ***"
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl --context=kind-pomelo-cluster wait --namespace default \
                --for=condition=Available \
                deployment/pomelo-cluster-entity-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "*** Strimzi Entity Operator setup complete. ***"
    fi

done

kubectl --context=kind-pomelo-cluster apply -f cluster-setup/kafka-streams/pomelo-config-map.yaml

kubectl --context=kind-pomelo-cluster apply -f cluster-setup/kafka-streams/pomelo-dplmt.yaml

kubectl --context=kind-pomelo-cluster apply -f cluster-setup/kafka-streams/pomelo-service.yaml

kubectl --context=kind-pomelo-cluster apply -f cluster-setup/kafka-streams/pomelo-hpa.yaml