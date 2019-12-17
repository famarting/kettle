#/bin/bash

kubectl config set-credentials kettle/localhost:7658 --username=kettle --password=kettle

kubectl config set-cluster localhost:7658 --insecure-skip-tls-verify=true --server=http://localhost:7658

kubectl config set-context default/localhost:7658/kettle --user=kettle/localhost:7658 --namespace=default --cluster=localhost:7658

kubectl config use-context default/localhost:7658/kettle

