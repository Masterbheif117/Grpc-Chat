package com.example.grpcchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.example.grpcchat.ChatProto.ChatMessage;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
 /*
     * ChatClient will connext to ChatServer it should send messages recieve broadcast messages from the server
     * 1. the client needs to create a channel to connect to the serveer
     * 2. the client will use a StreamObserver to send the chat messages to the server
     * 3. the client will use another StreamObserver to revieve chat messages from the server 
     * 4. keep a log of the chat history
     */
public class ChatClient {

    /*
     * how to implement?
     * First we need to set up a channel that can connceet to the server. the server needs to say the specific server address and prot
     * then we need to use that channel to create a stub for the chat service
     * we then need to create a method to read the user input from the console , the client should continously read and send it to the esrver
     * then we use streamobserver to send messages to the server
     * we ned to implement another stream bserver to handle incoming messages from the server
     * 
     */
      private final ManagedChannel channel;
      private final ChatServiceGrpc.ChatServiceStub asyncStub;
      private final List<ChatMessage> chatHistory = new ArrayList<>();

      public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter your name: ");
        String userName = scanner.nextLine();

        
        ChatClient client = new ChatClient("localhost", 31415);
        client.chat(userName);
        client.shutdown();
        scanner.close();
      }

      public ChatClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host,port).usePlaintext().build();
        asyncStub = ChatServiceGrpc.newStub(channel);
      }

      public void shutdown() throws InterruptedException{
        channel.shutdown().awaitTermination(12, TimeUnit.SECONDS);

      }


      public void chat(String user){
        StreamObserver<ChatMessage> responseObserver = new StreamObserver<ChatMessage>() {

            @Override
            public void onNext(ChatMessage message) {
              // add recieved message to chat history
              chatHistory.add(message);
             displayChatHistory(user);
             
             System.out.println("Enter a message (or 'exit' to quit)");
             System.out.println("");

            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
              System.out.println("...........................................");
                System.out.println("chat ended. ");
            }
            
        };

        StreamObserver<ChatMessage> reqObserver = asyncStub.chat(responseObserver);
        handleUserInput(reqObserver, user);
        
      }
      /*
       * function to display chat history. Contains a log of the messages that are being sent between clients
       */
      private void displayChatHistory(String user){
        System.out.println("--------------- Chat History ---------------");
        for(ChatMessage msg : chatHistory){
          if(msg.getUser().equals(user)){
            System.out.println("You: " + msg.getMessage());
          }
          else{
            System.out.println(msg.getUser() + ": " + msg.getMessage());
          }

        }
        System.out.println("");
        System.out.println("-----------------------------------------");
      }
      /*
       * Using a scanner to read input from terminal.
       * prompting for user input and then building a new message.
       */
      private void handleUserInput(StreamObserver<ChatMessage> reqObserver, String user){
        Scanner scanner = new Scanner(System.in);
        while(true){

          // System.out.println(".............................");
          System.out.println("Enter a message (or 'exit' to quit)");
          System.out.println("");
            String message = scanner.nextLine();
            if(message.equalsIgnoreCase("exit")){
                break;
            }
            ChatMessage chatMessage = ChatMessage.newBuilder().setUser(user).setMessage(message).build();
            
            reqObserver.onNext(chatMessage);
        }
        reqObserver.onCompleted();
        scanner.close();
      }

}
