# Messaging Pipeline
Messaging pipeline generates messages and allows the user to visualize them using the following components:

- Kafka and Zookeeper
- ELK stack
- Cassandra
- Grafana
- Jupyter notebooks + Spark
- Mock S3 

<img src="/src/main/resources/diagram/messages_flow.png" width=75%>

<img src="/src/main/resources/diagram/logs_flow.png" width=75%>

<img src="/src/main/resources/diagram/metrics_flow.png" width=75%>


## Installation Steps for Docker:

``` 
- cd messaging_pipeline/.integration
- ./setup-env.sh
- Run KafkaProducer
- Run EnrichmentProc
- Run ElasticsearchProc
- Run CassandraProc
- Run MetricProc
- Run LTProc
```

## Installation Steps for Elasticsearch:
- Add the following indexes:
```
- logstash*
- kafka*
```
- For better visualization, use the appropriate timestamp fields

## Installation Steps for Cassandra:
- Follow the instructions in `setup-cassandra.sh` file

## Installation Steps for Grafana:
- Follow the instructions in the `setup-grafana` file
- Add Elasticsearch as a datasource:
    - Use elasticsearch:9200 as the URL
    - Use \*kafka\* as the Index Name
    - Use version 7.0+
- Add Cassandra as a datasource:
    - Use cassandra:9042 as the Host
    - Use `kafka` as the keyspace

## Installation Steps for Jupyter:
- Run docker exec -it jupyter jupyter notebook list
- Copy the token provided to the clipboard to login
- Check connectivity using a notebook:
```
import urllib3

url = "http://s3:9090/messaging"
http = urllib3.PoolManager()
response = http.request('GET', url)
try:
    data = xmltodict.parse(response.data)
    print(data)
except:
    print("Failed to parse xml from response") 
```
