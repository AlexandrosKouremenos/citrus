#!/bin/bash
#set -o errexit

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

# Load application images in the cluster
kind load docker-image localhost:${reg_port}/pomelo:latest --name pomelo-cluster

# connect the registry to the cluster network if not already connected
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

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

# TODO: We need the persistent .yaml in case of pod failure.
kubectl apply -f kafka-ephemeral.yaml

# TODO: Find what to wait for.
echo "Waiting for Strimzi Entity Operator to complete its setup."
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl wait --namespace default \
                --for=condition=Available \
                deployment/my-cluster-entity-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "Strimzi Entity Operator setup complete."
    fi

done

#kubectl apply -f pomelo-kafka-topic.yaml

kubectl apply -f pomelo-secret.yaml

kubectl apply -f pomelo-dplmt.yaml

kubectl apply -f pomelo-service.yaml

## TODO: No need. Delete them when the time is right.
#kubectl apply -f pomelo-ingress-controller.yaml
#
#echo "Waiting for Nginx Ingress Controller to complete its setup."
#running=true
#while [ "$running" = true ]; do
#
#    sleep 5
#
#    complete=$(kubectl wait --namespace ingress-nginx \
#                 --for=condition=ready pod \
#                 --selector=app.kubernetes.io/component=controller \
#                 --timeout=-1s 2> /dev/null)
#
#    if grep -q "condition met" <<< "$complete"; then
#      running=false
#      echo "Nginx Ingress Controller setup complete."
#    fi
#
#done
#
#kubectl apply -f pomelo-allow-tcp.yaml
#
#kubectl apply -f pomelo-ingress.yaml
