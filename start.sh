#!/bin/bash

set -e

echo "Starting start.sh"

# 1. Build de app-tracing-management
echo "Building App Tracing Library..."
cd tracing-management-app
mvn clean install
cd ..

# 2. Build du Service A
echo "Building Service OTLP gRPC..."
cd service-otlp-grpc
./mvnw clean package -DskipTests
cd ..

# 3. Build du Service B
echo "Building Service Client..."
cd service-client
./mvnw clean package -DskipTests
cd ..

# 4 . Nettoyage
echo "Cleaning old containers"
docker rm -f jaeger-otel service-otlp-grpc service-client 2>/dev/null || true

# 5. Démarrage
echo "Starting Docker Compose"
docker compose up --build