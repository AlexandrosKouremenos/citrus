#!/bin/bash

kubectl create -f prometheus-operator-deployment.yaml

echo "Waiting for Prometheus Operator to complete its setup."
running=true
while [ "$running" = true ]; do

    sleep 5

    complete=$(kubectl wait --namespace default \
                --for=condition=Available \
                deployment/prometheus-operator \
                --timeout=-1s 2> /dev/null)

    if grep -q "condition met" <<< "$complete"; then
      running=false
      echo "Prometheus Operator setup complete."
    fi

done

kubectl apply -f prometheus-additional.yaml

kubectl apply -f strimzi-pod-monitor.yaml
kubectl apply -f prometheus-rules.yaml
kubectl apply -f prometheus.yaml