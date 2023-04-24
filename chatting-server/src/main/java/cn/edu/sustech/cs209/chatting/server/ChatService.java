package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class ChatService implements Runnable{
    private final ServerController controller;
    private final Socket socket;
    private volatile String userName;
    private Scanner sc;
    private PrintWriter pw;
    public ChatService(ServerController controller, Socket socket){
        this.controller=controller;
        this.socket=socket;
        userName="";
    }

    public String getUserName(){
        return userName;
    }

    @Override
    public void run() {
        try{
            try{
                sc=new Scanner(socket.getInputStream());
                pw=new PrintWriter(socket.getOutputStream());
                doService();
            }
            finally {
                socket.close();
                controller.removeChatService(this);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void doService() throws IOException{
        if(!sc.hasNext()||socket.isClosed()){
            socket.close();
            return;
        }
        String tmpName=sc.nextLine();
        while(controller.existsUser(tmpName)){
            pw.println("CREATE_USER_REJECTED");
            pw.flush();
            if(!sc.hasNext()||socket.isClosed()){
                socket.close();
                return;
            }
            tmpName=sc.nextLine();
        }
        userName=tmpName;
        pw.println("CREATE_USER_SUCCEEDED");
        pw.flush();
        while(true){
            if(!sc.hasNext()||socket.isClosed()){
                socket.close();
                return;
            }
            executeCommand(sc.nextLine());
        }
    }

    public void executeCommand(String command){
        switch (command) {
            case "CREATE_CHAT":
                int listLength=Integer.parseInt(sc.nextLine());
                Set<String> users=new TreeSet<>();
                while(listLength-->0){
                    users.add(sc.nextLine());
                }
                controller.createChat(users);
                break;
            case "GET_USER_LIST":
                Set<String> userList=controller.getUserList();
                pw.println(userList.size());
                for(String user:userList){
                    pw.println(user);
                }
                pw.flush();
                break;
            case "UPLOAD_MESSAGE":
                long chatId=Long.parseLong(sc.nextLine());
                long timeStamp=Long.parseLong(sc.nextLine());
                String content=sc.nextLine();
                controller.uploadMessage(new Message(timeStamp,userName,chatId,content));
                break;
            case "QUIT_CHAT":
                chatId=Long.parseLong(sc.nextLine());
                controller.quitChat(chatId,userName);
                break;
        }
    }

    public void downloadMessage(Message message){
        pw.print("DOWNLOAD_MESSAGE\n"+message.getSentBy()+"\n"+message.getSendTo()+"\n"+message.getTimestamp()+"\n"+message.getData()+"\n");
        pw.flush();
    }

    public void joinChat(Chat chat){
        pw.print("JOIN_CHAT\n"+chat.getChatId()+"\n"+chat.getUserList().size()+"\n");
        for(String userName:chat.getUserList()){
            pw.print(userName+"\n");
        }
        pw.flush();
    }

    public void quitChat(long chatId,String userName){
        pw.print("QUIT_CHAT\n"+chatId+"\n"+userName+"\n");
    }
}
