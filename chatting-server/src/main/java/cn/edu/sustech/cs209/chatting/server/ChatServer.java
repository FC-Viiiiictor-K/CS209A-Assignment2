package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int port=1453;
    private static final ServerController controller=new ServerController();
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        try(ServerSocket socket=new ServerSocket(port)){
            while(true){
                Socket acs=socket.accept();
                System.out.println("Client connected");
                ChatService chatService=new ChatService(controller,acs);
                controller.addChatService(chatService);
                Thread thread=new Thread(chatService);
                thread.start();
            }
        }
    }
}
