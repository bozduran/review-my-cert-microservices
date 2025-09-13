#!/bin/bash

# chmod +x deploy.sh

# Stop and remove containers, networks, images, and volumes
echo "Stopping and removing Docker containers..."
docker compose down -v --remove-orphans

export CONFIG_SERVER_USR=dev-usr
export CONFIG_SERVER_PWD=dev-pwd
echo $CONFIG_SERVER_USR
echo $CONFIG_SERVER_PWD

# Build Maven project without running tests
echo "Building Maven project..."
mvn clean package -DskipTests

# Build Docker images without cache
echo "Building Docker images..."
docker compose build --no-cache

# Start Docker containers
echo "Starting Docker containers..."
docker compose up -d

curl -k -u writer:secret-writer \
  -d "grant_type=client_credentials" \
  -d "scope=product:read product:write" \
  https://localhost:8443/oauth2/token -s | jq


unset ACCESS_TOKEN
ACCESS_TOKEN=$(curl -k https://writer:secret-writer@localhost:8443/oauth2/token -d grant_type=client_credentials -d scope="product:read product:write" -s | jq -r .access_token)
echo $ACCESS_TOKEN

curl -H "Authorization: Bearer $ACCESS_TOKEN" -k https://localhost:8443/certificate/1 -w "%{http_code}\n" -o /dev/null -s