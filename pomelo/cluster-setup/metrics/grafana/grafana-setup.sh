#!/bin/bash

project_dir=$(pwd | grep -o '.*citrus')

if [ -z "${project_dir}" ]; then
    echo "Citrus directory not found."
    exit 1
fi
source "${project_dir}"/citrus-paths.sh

reg_port='5001'

docker pull grafana/grafana:7.3.7

docker tag grafana/grafana:7.3.7 localhost:${reg_port}/grafana/grafana:7.3.7

docker push localhost:${reg_port}/grafana/grafana:7.3.7

cd "${STRIMZI_PATH}" || exit

sed -i "s|image: grafana/grafana:7.3.7|image: localhost:${reg_port}/grafana/grafana:7.3.7|" examples/metrics/grafana-install/grafana.yaml

kubectl --context=kind-pomelo-cluster apply -f examples/metrics/grafana-install/grafana.yaml

echo "*** Waiting for Grafana to complete its setup. ***"
running=true
while [ "$running" = true ]; do

  sleep 5

  complete=$(kubectl --context=kind-pomelo-cluster wait --namespace default \
    --for=condition=Available \
    deployment/grafana \
    --timeout=-1s 2>/dev/null)

  if grep -q "condition met" <<<"$complete"; then
    running=false
    echo "*** Grafana setup complete. ***"
  fi

done

kubectl --context=kind-pomelo-cluster port-forward svc/grafana 3000:3000 & xdg-open http://localhost:3000
# Prometheus server: http://prometheus-operated.default.svc:9090