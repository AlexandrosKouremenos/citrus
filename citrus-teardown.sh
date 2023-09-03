#!/bin/bash

kind delete cluster --name sunki-cluster

kind delete cluster --name pomelo-cluster

docker stop citrus-registry && docker rm citrus-registry