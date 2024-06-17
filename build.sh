#!/bin/bash

# Stop and remove any running containers
docker-compose down --remove-orphans

# Build the Docker images
docker-compose build

# Start the server container in detached mode to run in the background
docker-compose up -d chatserver

# to interact with the client 
echo "to run the client service interactively use in a new terminal (run this command in each new terminal you open for a client):"
echo "docker-compose run grpc-chat-client"


