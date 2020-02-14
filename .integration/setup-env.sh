#!/usr/bin/env bash

COMPOSE_VERSION=$(docker-compose --version)
DOCKER_VERSION=$(docker --version)

runCommand() {
  echo $1
  until $2
  do
    result="$?"
    if [[ "$result" == "1" ]]; then
      echo "Bad status code: $result. Trying again."
    else
      # If it is some unknown status code, die.
      exit 1
    fi
  done
}

# Start the docker compose file
echo "Running docker compose up. Docker version $DOCKER_VERSION. Compose version $COMPOSE_VERSION. "

echo "If it's up, we're bringing it down..."
docker-compose down
echo "Bringing it up!"
docker-compose up -d

if [[ "$?" == "1" ]]; then
  echo "Failed to start docker images."
  exit 1
fi

# Create the topic
runCommand \
  "Creating kafka topic" \
  "docker-compose exec kafka kafka-topics --create --topic kafka-example --partitions 2 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181"
