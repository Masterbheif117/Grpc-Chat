# gRPC Chat Application

This is a simple project that gets me comfortable with gRPC and docker. The application has a server and client component. It allows for as many clients to send messages to the server and display the message from other clients. It uses bi-directional streaming.

## Instructions

1. **Clone the repository**
    -after cloning the repository cd into grpc-chat-project
2. **Run the build script**
    -run the build script as follows "./build.sh"
3. **Run the client/clients**
    -The server should be running. After the server is running, you can start the client or clients on a new terminal.
    -For each new client, open a new terminal
    -The command to run a client is : "docker-compose run grpc-chat-client"
## Extra Info

The client trys to connect to local host however it needs to connect to the server container which is called "chatserver" and is defined in the .yml file. Which creates its own private docker network.



    