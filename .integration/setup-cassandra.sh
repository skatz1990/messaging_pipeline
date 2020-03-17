# !/bin/bash

POD=$(k get pod -l app=cassandra -o jsonpath="{.items[0].metadata.name}")
k exec -it $POD bash
cqlsh
CREATE KEYSPACE kafka WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
CREATE TABLE kafka.messages(
   msg_id int PRIMARY KEY,
   msg_data text
   );

