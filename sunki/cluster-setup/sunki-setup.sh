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
kind create cluster --config sunki-cluster.yaml

docker push localhost:${reg_port}/sunki-mosquitto:latest

# connect the registry to the cluster network if not already connected
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

# Load application images in the cluster
kind load docker-image localhost:${reg_port}/sunki-mosquitto:latest --name sunki-cluster

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

echo "Waiting for Strimzi Cluster Operator to complete its setup."

running=true
while [ "$running" = true ]; do
    sleep 5

    complete=$(kubectl wait --namespace default \
                --for=condition=Available \
                deployment/strimzi-cluster-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "Strimzi Cluster Operator setup complete."
    fi

done

cd ~/Repos/citrus/sunki/cluster-setup/ || exit

kubectl apply -f mosquitto/sunki-mosquitto-secret.yaml

kubectl apply -f mosquitto/sunki-mosquitto-config-map.yaml

kubectl apply -f mosquitto/sunki-mosquitto-dplmt.yaml

kubectl apply -f mosquitto/sunki-mosquitto-service.yaml

kubectl apply -f mosquitto/sunki-mosquitto-ingress-controller.yaml

echo "Waiting for Nginx Ingress Controller to complete its setup."
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl wait --namespace ingress-nginx \
                 --for=condition=ready pod \
                 --selector=app.kubernetes.io/component=controller \
                 --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "Nginx Ingress Controller setup complete."
    fi

done

kubectl apply -f mosquitto/sunki-mosquitto-allow-tcp.yaml

kubectl apply -f mosquitto/sunki-mosquitto-ingress.yaml

docker push localhost:${reg_port}/sunki-connector:latest

kind load docker-image localhost:${reg_port}/sunki-connector:latest --name sunki-cluster

bootstrapServers=$(kubectl --context=kind-pomelo-cluster get kafka pomelo-cluster -o=jsonpath='{.status.listeners[?(@.name=="external")].bootstrapServers}{"\n"}')

echo "$bootstrapServers"

sed "s/{{kafka-external-bootstrap}}/$bootstrapServers/g" kafka-connector/sunki-connector-kafka-connect.yaml > kafka-connector/temp.yaml

kubectl apply -f kafka-connector/temp.yaml

rm kafka-connector/temp.yaml

kubectl apply -f kafka-connector/sunki-connector-kafka-connector.yaml