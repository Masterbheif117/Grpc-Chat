# gRPC Chat Application

This is a simple project that gets me comfortable with gRPC and docker. The application has a server and client component. It allows for as many clients to send messages to the server and display the message from other clients. It uses bi-directional streaming.

## Instructions

1. **Clone the repository**
    -after cloning the repository cd into grpc-chat-project
2. **Run the build script**
    -run the build script as follows "./build.sh"
3. **Run the client/clients**
    -The server should be running. After the server is running, you can start the client or clients on a new terminal.
    -For each new client, there should be a new terminal
    -The command to run a client is : "docker run -it --rm --network host grpc-chat-client"
## Extra Info

The server runs on localhost:31415, however to make sure all the clients are able to talk the server as well as talk to one another I used Dockers host network mode. 



    