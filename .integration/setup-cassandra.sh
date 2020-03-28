# !/bin/bash

# For Kubernetes:
POD=$(kubectl get pod -l app=cassandra -o jsonpath="{.items[0].metadata.name}")
kubectl exec -it $POD bash

# For Docker
docker exec -it cassandra bash


cqlsh
CREATE KEYSPACE kafka WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
CREATE TABLE kafka.tweets(
    date text,
    tweet text,
    firstName text,
    lastName text,
    PRIMARY KEY ((firstName, lastName), date)
   );