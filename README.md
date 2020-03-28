# kafka_playground
This repository contains the following components:

- Kafka infrastructure (including Zookeeper)
- Kafka Consumer
- Kafka Producer
- Elastic Search 
- Kibana
- Cassandra
docker exec cassandra
![Software Architecture](https://github.com/skatz1990/kafka_playground/blob/master/src/main/resources/diagram/Diagram.png?raw=true)

Future Plans:
- [x] Add Producer
- [x] Add Consumer
- [x] Add Elastic Search
- [x] Integrate Kibana
- [x] Add Cassandra
- [x] Improve Readme with an ifrastructure diagram
- [ ] Add installation instructions in Readme
- [x] Change Producer to produce a more realistic data, for instance - mouse movement from the user, or keyboard input
- [ ] Integrate Spark?
- [ ] Use Helm Charts

## Installation Steps for Docker:

``` 
- cd kafka_playground/.integration
- ./setup-env.sh
- docker exec -it cassandra cqlsh
- CREATE KEYSPACE kafka WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
- CREATE TABLE kafka.tweets(
    date text,
    tweet text,
    firstName text,
    lastName text,
    PRIMARY KEY ((firstName, lastName), date)
   );
- Run KafkaProducer
- Run ElasticsearchProc
- Run CassandraProc
```