#!/bin/bash

script_dir=$(pwd)

source "${script_dir}"/citrus-paths.sh

SUNKI_LOGS="\e[33m" # Yellow
SUNKI_BOLD_LOGS="\e[1;33mSunki: \e[0m" # Yellow & Bold

POMELO_LOGS="\e[36m" # Cyan
POMELO_BOLD_LOGS="\e[1;36mPomelo: \e[0m" # Cyan & Bold

PROMETHEUS_LOGS="\e[32m" # Green
PROMETHEUS_BOLD_LOGS="\e[1;32mPrometheus: \e[0m" # Green & Bold

GRAFANA_LOGS="\e[35m" # Magenta
GRAFANA_BOLD_LOGS="\e[1;35mGrafana: [\e[0m" # Magenta & Bold

RESET_COLOR="\e[0m"

# Function to run Sunki setup script.
run_sunki() {
  cd "$script_dir"/sunki/cluster-setup || exit
  ./sunki-setup.sh 2>&1 | while IFS= read -r line; do
    echo -e "${SUNKI_BOLD_LOGS}${SUNKI_LOGS}[${line}]${RESET_COLOR}"
  done
}

# Function to run Pomelo setup script.
run_pomelo() {
  cd "$script_dir"/pomelo/cluster-setup || exit
  ./pomelo-setup.sh 2>&1 | while IFS= read -r line; do
      echo -e "${POMELO_BOLD_LOGS}${POMELO_LOGS}[${line}]${RESET_COLOR}"
  done
}

# Function to run Prometheus setup script.
run_prometheus() {
  cd "$script_dir"/pomelo/cluster-setup/metrics/prometheus || exit
  ./prometheus-setup.sh 2>&1 | while IFS= read -r line; do
      echo -e "${PROMETHEUS_BOLD_LOGS}${PROMETHEUS_LOGS}[${line}]${RESET_COLOR}"
  done
}

# Function to run Grafana setup script.
run_grafana() {
  cd "$script_dir"/pomelo/cluster-setup/metrics/grafana || exit
  ./grafana-setup.sh 2>&1 | while IFS= read -r line; do
     echo -e "${GRAFANA_BOLD_LOGS}${GRAFANA_LOGS}[${line}]${RESET_COLOR}"
  done
}

# Create registry container unless it already exists.
reg_name='citrus-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --name "${reg_name}" \
    registry:2
fi

# Setup Sunki and Pomelo cluster.
(run_sunki && echo -e "${SUNKI_LOGS}*** Sunki cluster setup complete. ***${RESET_COLOR}") &
(run_pomelo && echo -e "${POMELO_LOGS}*** Pomelo cluster setup complete. ***${RESET_COLOR}") &

wait

# Setup Prometheus.
run_prometheus && echo -e "${PROMETHEUS_LOGS}*** Prometheus setup complete. ***${RESET_COLOR}"

# Setup Grafana. Prometheus server: http://prometheus-operated.default.svc:9090
run_grafana && echo -e "${GRAFANA_LOGS}*** Grafana setup complete. ***${RESET_COLOR}"