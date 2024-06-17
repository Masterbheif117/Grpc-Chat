package com.example.grpcchat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.grpcchat.ChatProto.ChatMessage;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/*
 * ChatServer is responsible for creating gRPC server to handle chat messages,
 * broadcasting messages to all clients that are connected
 * will also manage client connections 
 */
public class ChatServer {
    public Server server;

    /*
     * Main method to run chatserver
     * intialized and starts the server, then waits for shutdwon(ctrl+c)
     * 
     * 
     * @param args command line argument
     * 
     * @throws IOexception if there is an I/O error during server startup
     * 
     * @throws InterruptedExceptin if server is interreupted while running
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ChatServer server = new ChatServer();
        server.start();
        server.blockUntilShutdown();
    }

    /*
     * starts gRPC server and connects it to port 31415
     * 
     * @throws IOException if there is an error starting the server
     */
    private void start() throws IOException {
        server = ServerBuilder.forPort(31415).addService(new ChatServiceImpl()).build().start();
        System.out.println(".......................................");
        System.out.println("Server started at: " + server.getPort());
    }

    /*
     * Blocks until server is shut down
     * 
     * @throws interruptedException if server is interrupted
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /*
     * implementing chat service from prooto file
     * 
     * @throws interrputedException if server is interrupted
     */
    private class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
        private List<StreamObserver<ChatMessage>> clients = new CopyOnWriteArrayList<>();

        /*
         * handles bidirectional streaming for chat messages
         * adds new client to list and broacast received messages to all clients
         * 
         * 
         * @param responseobserver the streamobserver to send to client
         * 
         * @return a streamobserver to recieve message from list
         */
        @Override
        public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
            // add new client to list
            clients.add(responseObserver);
            return new StreamObserver<ChatMessage>() {
                @Override
                public void onNext(ChatMessage chatMessage) {
                    // broadcast message to all clients
                    for (StreamObserver<ChatMessage> client : clients) {
                        client.onNext(chatMessage);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    // remove clients from list
                    clients.remove(responseObserver);
                }

                @Override
                public void onCompleted() {
                    // remoove client from list and notify client
                    clients.remove(responseObserver);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
