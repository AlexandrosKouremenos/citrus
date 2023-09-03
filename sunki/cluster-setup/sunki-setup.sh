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
kind create cluster --config sunki-cluster.yaml

docker tag sunki-mosquitto:latest localhost:${reg_port}/sunki-mosquitto:latest

docker push localhost:${reg_port}/sunki-mosquitto:latest

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

kubectl --context=kind-sunki-cluster create clusterrolebinding strimzi-cluster-operator-namespaced \
        --clusterrole=strimzi-cluster-operator-namespaced \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl --context=kind-sunki-cluster create clusterrolebinding strimzi-cluster-operator-watched \
        --clusterrole=strimzi-cluster-operator-watched \
        --serviceaccount \
         default:strimzi-cluster-operator

kubectl --context=kind-sunki-cluster create clusterrolebinding strimzi-cluster-operator-entity-operator-delegation \
        --clusterrole=strimzi-entity-operator \
        --serviceaccount \
        default:strimzi-cluster-operator

kubectl --context=kind-sunki-cluster create -f install/cluster-operator/ -n default

echo "*** Waiting for Strimzi Cluster Operator to complete its setup. ***"

running=true
while [ "$running" = true ]; do
    sleep 5

    complete=$(kubectl --context=kind-sunki-cluster wait --namespace default \
                --for=condition=Available \
                deployment/strimzi-cluster-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "*** Strimzi Cluster Operator setup complete. ***"
    fi

done

cd "${SUNKI_PATH}" || exit

kubectl --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-config-map.yaml

kubectl --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-dplmt.yaml

kubectl --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-service.yaml

kubectl --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-ingress-controller.yaml

echo "*** Waiting for Nginx Ingress Controller to complete its setup. ***"
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl --context=kind-sunki-cluster wait --namespace ingress-nginx \
                 --for=condition=ready pod \
                 --selector=app.kubernetes.io/component=controller \
                 --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "*** Nginx Ingress Controller setup complete. ***"
    fi

done

kubectl --context=kind-sunki-cluster --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-allow-tcp.yaml

kubectl --context=kind-sunki-cluster --context=kind-sunki-cluster apply -f cluster-setup/mosquitto/sunki-mosquitto-ingress.yaml

docker tag sunki-connector:latest localhost:${reg_port}/sunki-connector:latest

docker push localhost:${reg_port}/sunki-connector:latest

echo "*** Waiting for Pomelo to complete its setup. ***"

bootstrapServers=""
while [ -z "$bootstrapServers" ]; do

  bootstrapServers=$(kubectl --context=kind-pomelo-cluster get kafka pomelo-cluster -o=jsonpath='{.status.listeners[?(@.name=="external")].bootstrapServers}{"\n"}')
  sleep 5

done

echo "*** Pomelo setup complete. Found bootstrap servers: $bootstrapServers ***"

sed -i "s|bootstrapServers: .*|bootstrapServers: $bootstrapServers|g" cluster-setup/kafka-connector/sunki-connector-kafka-connect.yaml

kubectl --context=kind-sunki-cluster apply -f cluster-setup/kafka-connector/sunki-connector-kafka-connect.yaml

kubectl --context=kind-sunki-cluster apply -f cluster-setup/kafka-connector/sunki-connector-kafka-connector.yaml