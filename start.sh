#!/bin/bash

echo "🚀 Construction des projets..."

# Build du Service A
echo "📦 Building Service OTLP gRPC..."
cd service-otlp-grpc
./mvnw clean package -DskipTests
cd ..

# Build du Service B
echo "📦 Building Service Client..."
cd service-client
./mvnw clean package -DskipTests
cd ..

echo "🐳 Démarrage de Docker Compose..."
docker compose down
docker compose up --build