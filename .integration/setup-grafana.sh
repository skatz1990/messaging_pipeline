# !/bin/bash
docker exec -it grafana bash

cd /var/lib/grafana/plugins && \
wget https://github.com/HadesArchitect/grafana-cassandra-source/releases/download/0.1.7/cassandra-datasource-0.1.7.zip && \
unzip cassandra-datasource-0.1.7.zip

exit
docker restart grafana
