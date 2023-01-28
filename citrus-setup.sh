#!/bin/bash
set -o errexit

# create registry container unless it already exists
reg_name='citrus-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --name "${reg_name}" \
    registry:2
fi

# create a cluster with the local registry enabled in containerd
kind create cluster --config sunki/cluster-setup/sunki-cluster.yaml

kubectl apply -f sunki/cluster-setup/sunki-secret.yaml

docker push localhost:${reg_port}/sunki:latest

# Load application images in the cluster
#kind load docker-image localhost:${reg_port}/sunki:latest --name sunki-cluster

# connect the registry to the cluster network if not already connected
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

kubectl apply -f sunki/cluster-setup/sunki-config-map.yaml

kubectl apply -f sunki/cluster-setup/sunki-dplmt.yaml

kubectl apply -f sunki/cluster-setup/sunki-service.yaml

kubectl apply -f sunki/cluster-setup/sunki-ingress-controller.yaml

running=true
while [ "$running" = true ]; do

    echo "Waiting for nginx-ingress-controller to complete its setup."
    sleep 5

    complete=$(kubectl wait --namespace ingress-nginx \
                 --for=condition=ready pod \
                 --selector=app.kubernetes.io/component=controller \
                 --timeout=-1s)

    if grep -q "condition met" <<< "$complete"; then
      running=false
    fi

done

kubectl apply -f sunki/cluster-setup/sunki-allow-tcp.yaml

kubectl apply -f sunki/cluster-setup/sunki-ingress.yaml
