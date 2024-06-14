package com.example.grpcchat;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.example.grpcchat.ChatProto.ChatMessage;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/*implementing the server side for the chat implementation.
Includes a private class which implements the functions made from the proto file completing the chat service
 */  
public class ChatServer {
    // creating a instance of a grpc server
    public Server server;


    public static void main(String[] args) throws IOException, InterruptedException{
        // create and start the chatserver to listen to a specifc port
        ChatServer server = new ChatServer();
        server.start();
        server.waitForShutdownCommand(); // as well as stopping
        server.stop();
    }
    // trying to create a server that listens on a specific port (31415)
    private void start() throws IOException {
        // Build and start the gRPC server
        // starts the server with the chatserverice implementation 
        server = ServerBuilder.forPort(31415).addService(new ChatServiceImpl()).build().start();
        System.out.println(".......................................");
        System.out.println("Server started at: " + server.getPort());

    }
     // Wait for the "exit" command to shut down the server
    private void waitForShutdownCommand() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equalsIgnoreCase("exit")) {
                break;
            }
        }
        scanner.close();
    }

    // Stop the server gracefully
    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            System.out.println("Server shutting down....");
        }
    }
    

    /*
     * implementation of the chatservice functions defined in the proto file 
     */
    private class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase{
        // tracking each client in a response which is done through a list for ease of access 
        private List<StreamObserver<ChatMessage>> clients = new CopyOnWriteArrayList<>();
        /*
         * the chat method is for bidirectional streaming so we
         * need to override it so we can handle both incomnig and outcoming messages
         * there is a streamobserver from the gRPC library which we can use to handle
         * client messages.We pass it as a paramaeter as a way for the server to send back a message to a client hence why it is a response
         */
        @Override
        public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
            // add new client to the list
            clients.add(responseObserver);
            return new StreamObserver<ChatMessage>(){
                
                //make sure the message gets to all clients, handling incomning message
                @Override
                public void onNext(ChatMessage chatMessage) {
                    //we weant to make sure that the message is given to all clients, and broadcasting them 
                    for(StreamObserver<ChatMessage> client : clients){
                        client.onNext(chatMessage);
                    }
                }
                
                @Override
                // if client disconnects or gets a error remove from list 
                public void onError(Throwable t) {
                    clients.remove(responseObserver);
                }


                /*
                 * once the stream is complete we still want to remove a client from the list and then
                 * update response observer to be completed by calling the super method
                 */
                @Override
                public void onCompleted() {
                    //make sure streams are being handled and completed 
                    clients.remove(responseObserver);
                    // notify client that the stream is completed 
                    responseObserver.onCompleted();
                }
                
            };
        }
    }
}