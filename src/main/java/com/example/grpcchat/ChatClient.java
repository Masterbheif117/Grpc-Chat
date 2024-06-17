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
 * ChatClient is connecting to the ChatServer. It sends messages, receives broadcast messaging 
 * and maintains a chat histroy 
 */
public class ChatClient {
  private final ManagedChannel channel;
  private final ChatServiceGrpc.ChatServiceStub asyncStub;
  private final List<ChatMessage> chatHistory = new ArrayList<>();

  /*
   * Main method to run the ChatClient
   * Will prompt the user for their name and start the chat session
   * 
   * @param args Command line arguments
   * 
   * @throws InterruptedException if the shutdown is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Please enter your name: ");
    String userName = scanner.nextLine();

    ChatClient client = new ChatClient("chatserver", 31415); // Connect to 'chatserver' service
    client.chat(userName);
    client.shutdown();
    scanner.close();
  }

  /*
   * Constructor to create init a ChatClient
   * 
   * @param host the host address of the ChatServer
   * 
   * @param port The port number of the ChatServer it will connect to
   */
  public ChatClient(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    asyncStub = ChatServiceGrpc.newStub(channel);
  }

  /*
   * shuts down the client channel
   * 
   * @throws InterruptedException if shutdown is interrputed
   */
  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(12, TimeUnit.SECONDS);
  }

  /*
   * starts a chat session for the user.
   * sets up response observers for recieving messages
   * and handles user input for sending message
   * 
   * 
   * @param user the usernmae of the chat participant
   */
  public void chat(String user) {
    // observer to handle incoming message from the server
    StreamObserver<ChatMessage> responseObserver = new StreamObserver<ChatMessage>() {

      @Override
      public void onNext(ChatMessage message) {

        // add received message to chat history and display
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
        System.out.println("Chat ended.");
      }
    };
    // observer to handle sending message to server
    StreamObserver<ChatMessage> reqObserver = asyncStub.chat(responseObserver);
    handleUserInput(reqObserver, user);
  }

  /*
   * display chat history, including all particpant
   */
  private void displayChatHistory(String user) {
    System.out.println("--------------- Chat History ---------------");
    for (ChatMessage msg : chatHistory) {
      if (msg.getUser().equals(user)) {
        System.out.println("You: " + msg.getMessage());
      } else {
        System.out.println(msg.getUser() + ": " + msg.getMessage());
      }
    }
    System.out.println("");
    System.out.println("-----------------------------------------");
  }

  /*
   * handle user input for sending message
   * read message from console and send to server
   * 
   * @param reqobserver stream observer for sending message to server
   * 
   * @user usernmae of chat particaptn
   */
  private void handleUserInput(StreamObserver<ChatMessage> reqObserver, String user) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.println("Enter a message (or 'exit' to quit)");
      System.out.println("");
      String message = scanner.nextLine();
      if (message.equalsIgnoreCase("exit")) {
        break;
      }
      // build and send chat message
      ChatMessage chatMessage = ChatMessage.newBuilder().setUser(user).setMessage(message).build();
      reqObserver.onNext(chatMessage);
    }
    reqObserver.onCompleted();
    scanner.close();
  }
}
