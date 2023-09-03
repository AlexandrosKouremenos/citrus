#!/bin/bash

project_dir=$(pwd | grep -o '.*citrus')

if [ -z "${project_dir}" ]; then
    echo "Citrus directory not found."
    exit 1
fi
source "${project_dir}"/citrus-paths.sh

reg_port='5001'

kubectl --context=kind-pomelo-cluster create -f prometheus-operator-deployment.yaml

echo "*** Waiting for Prometheus Operator to complete its setup. *** "
running=true
while [ "$running" = true ]; do

  sleep 5

  complete=$(kubectl --context=kind-pomelo-cluster wait --namespace default \
    --for=condition=Available \
    deployment/prometheus-operator \
    --timeout=-1s 2>/dev/null)

  if grep -q "condition met" <<<"$complete"; then
    running=false
    echo "*** Prometheus Operator setup complete. ***"
  fi

done

cd "${STRIMZI_PATH}" || exit

kubectl --context=kind-pomelo-cluster apply -f examples/metrics/prometheus-additional-properties/prometheus-additional.yaml
sleep 2

sed -i 's|myproject|default|' examples/metrics/prometheus-install/strimzi-pod-monitor.yaml
kubectl --context=kind-pomelo-cluster apply -f examples/metrics/prometheus-install/strimzi-pod-monitor.yaml
sleep 2

kubectl --context=kind-pomelo-cluster apply -f examples/metrics/prometheus-install/prometheus-rules.yaml
sleep 2

sed -i 's|namespace: myproject|namespace: default|' examples/metrics/prometheus-install/prometheus.yaml
kubectl --context=kind-pomelo-cluster apply -f examples/metrics/prometheus-install/prometheus.yaml
sleep 2

docker pull registry.k8s.io/prometheus-adapter/prometheus-adapter:v0.11.0

docker tag registry.k8s.io/prometheus-adapter/prometheus-adapter:v0.11.0 localhost:${reg_port}/prometheus-adapter/prometheus-adapter:v0.11.0

docker push localhost:${reg_port}/prometheus-adapter/prometheus-adapter:v0.11.0

cd "${POMELO_PATH}"/cluster-setup/metrics/prometheus || exit

# Setting up Prometheus Adapter and external metrics.
adapter_path="prometheus-adapter"
if [ ! -d "$adapter_path" ]; then

  mkdir "$adapter_path"
  cd "$adapter_path" || exit

  wget 'https://github.com/kubernetes-sigs/prometheus-adapter/archive/refs/heads/release-0.11.zip' &&
    unzip release-0.11.zip &&
    mv prometheus-adapter-release-0.11/deploy/manifests . &&
    rm -rf prometheus-adapter-release-0.11 release-0.11.zip

  sed -i 's/namespace: monitoring/namespace: default/g' manifests/*.yaml

  sed -i '/config.yaml:/a\
    externalRules:\
      - seriesQuery: '\''{topic!="",__name__=~"kafka_consumergroup_lag"}'\''\
        resources:\
          template: <<.Resource>>\
        metricsQuery: sum(min_over_time(kafka_consumergroup_lag{<< range $key, $value :=.LabelValuesByName >><< if ne $key "namespace" >><< $key >>="<< $value >>",<<end >><< end >>}[1h])) by (topic,consumergroup)\
        name:\
          as: kafka_lag' manifests/config-map.yaml

  # TODO: Find how to replace 5001 with $reg_port in the following command.
  sed -i 's|prometheus-url=https://prometheus.monitoring.svc:9090|prometheus-url=http://prometheus-operated.default.svc:9090|;
  s|replicas: 2|replicas: 1|;
  s|image: registry.k8s.io/prometheus-adapter/prometheus-adapter:v0.11.0|image: localhost:5001/prometheus-adapter/prometheus-adapter:v0.11.0|;
  /- args:/a\
        - --v=10 #logs every requests' manifests/deployment.yaml

  sed -i 's|metrics.k8s.io|external.metrics.k8s.io|' manifests/cluster-role-metrics-server-resources.yaml

  sed -i 's|hpa-controller-custom-metrics|hpa-controller-external-metrics|;
  s|custom-metrics-server-resources|external-metrics-server-resources|' manifests/cluster-role-binding-hpa-custom-metrics.yaml

  sed -i 's|metrics.k8s.io|external.metrics.k8s.io|' manifests/cluster-role-aggregated-metrics-reader.yaml

  sed -i 's|name: v1beta1.metrics.k8s.io|name: v1beta1.external.metrics.k8s.io|;
  s|group: metrics.k8s.io|group: external.metrics.k8s.io|' manifests/api-service.yaml

  kubectl --context=kind-pomelo-cluster create -f manifests/

else

  cd "$adapter_path" || exit
  kubectl --context=kind-pomelo-cluster create -f manifests/

fi
