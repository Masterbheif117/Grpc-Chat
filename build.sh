#!/bin/bash

# clean and build maven project

echo "cleaning and building maven project "
mvn clean install

# build the docker images for server and client side
echo "building Docker images for server and client ..."
docker build -t grpc-chat-server -f Dockerfile.server .
docker build -t grpc-chat-client -f Dockerfile.client .


# run the server container
# -network lets the us use the host network, allows local host to communicate w host
echo "Running the server container .."
docker run -d --name grpc-server --network host grpc-chat-server

echo "server is now running. You can start the client/clients using the following command in a new terminal (for each client make a new terminal)"
# -it allows us to use the container within the terminal
# --rm removes the container once we type in exit and shutdown the container
echo "docker run -it --rm --network host grpc-chat-client"



