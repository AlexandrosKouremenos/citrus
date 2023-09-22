#!/bin/bash

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

docker push localhost:${reg_port}/pomelo:latest

# connect the registry to the cluster network if not already connected
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

# Load application images in the cluster
kind load docker-image localhost:${reg_port}/pomelo:latest --name pomelo-cluster

cd ~/Utilities/strimzi-0.34.0/ || exit

sed -i 's/namespace: .*/namespace: default/' install/cluster-operator/*RoleBinding*.yaml

kubectl create clusterrolebinding strimzi-cluster-operator-namespaced \
        --clusterrole=strimzi-cluster-operator-namespaced \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl create clusterrolebinding strimzi-cluster-operator-watched \
        --clusterrole=strimzi-cluster-operator-watched \
        --serviceaccount \
         default:strimzi-cluster-operator

kubectl create clusterrolebinding strimzi-cluster-operator-entity-operator-delegation \
        --clusterrole=strimzi-entity-operator \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl create -f install/cluster-operator/ -n default

cd ~/Repos/citrus/pomelo/cluster-setup/ || exit

# TODO: We need the persistent .yaml in case of failure.
kubectl apply -f kafka-cluster/kafka-ephemeral.yaml

echo "Waiting for Strimzi Entity Operator to complete its setup."
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl wait --namespace default \
                --for=condition=Available \
                deployment/pomelo-cluster-entity-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "Strimzi Entity Operator setup complete."
    fi

done

kubectl apply -f kafka-streams/pomelo-secret.yaml

kubectl apply -f kafka-streams/pomelo-dplmt.yaml

kubectl apply -f kafka-streams/pomelo-service.yaml

kubectl apply -f kafka-streams/pomelo-hpa.yaml