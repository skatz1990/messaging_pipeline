# !/bin/bash

# POD=$(k get pod -l app=cassandra -o jsonpath="{.items[0].metadata.name}")
# kubectl exec -it $POD bash
docker exec -it cassandra bash
cqlsh
CREATE KEYSPACE kafka WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
CREATE TABLE kafka.tweets(
   msg_id int PRIMARY KEY,
   msg_data text
   );