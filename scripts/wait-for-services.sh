#!/bin/bash

# Wait for PostgreSQL
echo "Waiting for PostgreSQL..."
until pg_isready -h localhost -p 5433 -U postgres; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done
echo "PostgreSQL is up and running"

# Wait for Elasticsearch
echo "Waiting for Elasticsearch..."
until curl -f http://localhost:9201/_cluster/health; do
  echo "Elasticsearch is unavailable - sleeping"
  sleep 5
done
echo "Elasticsearch is up and running"

# Wait for application
echo "Waiting for application..."
until curl -f http://localhost:8081/actuator/health; do
  echo "Application is unavailable - sleeping"
  sleep 5
done
echo "Application is up and running"

echo "All services are ready!" 