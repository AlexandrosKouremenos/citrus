# Grafana

Grafana is a multi-platform open source analytics and interactive visualization web application.

## Setup

When [grafana-setup.sh](grafana-setup.sh) script end its execution, it will open a browser tab (localhost:3000) with the Grafana dashboard.

When prompted for credentials on the dashboard, for username and password use `admin`.

From the menu on the left, click on the gear icon to open the Configuration page.

Then, click on the `Data Sources` tab and add a new data source.

Select `Prometheus` as the type and use `http://prometheus-operated.default.svc:9090` as the URL.

Click on `Save & Test` to save the data source.

## Dashboards

From the menu on the left, click on the plus icon to open the Create page.

Then, click on the `Import` tab and import (copy & paste) the following dashboards in .json format:

- [Kafka](strimzi-kafka.json)
- [Kafka-Exporter](strimzi-kafka-exporter.json)
- [Kafka-Operator](strimzi-operators.json)
- [Zookeeper](strimzi-zookeeper.json)