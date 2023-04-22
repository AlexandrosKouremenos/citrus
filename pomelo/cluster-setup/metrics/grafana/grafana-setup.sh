#!/bin/bash
kubectl apply -f grafana.yaml
kubectl port-forward svc/grafana 3000:3000
# Prometheus server: http://prometheus-operated:9090