package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

public class ReceiveMessageThread implements Runnable{
  private final Controller controller;
  private final Socket socket;
  private final Scanner sc;
  private final PrintWriter pw;
  public ReceiveMessageThread(Controller controller,Socket socket,Scanner sc,PrintWriter pw){
    this.controller=controller;
    this.socket=socket;
    this.sc=sc;
    this.pw=pw;
  }
  @Override
  public void run() {
    while (true) {
      if (!sc.hasNext() || socket.isClosed()) {
        return;
      }
      executeCommand(sc.nextLine());
    }
  }
  
  private void executeCommand(String command){
    System.out.println(command);
    switch(command){
      case "SET_ONLINE_CNT":
        int onlineCnt=Integer.parseInt(sc.nextLine());
        controller.setOnlineCnt(onlineCnt);
        break;
      case "DOWNLOAD_MESSAGE":
        String sendBy=sc.nextLine();
        long chatId=Long.parseLong(sc.nextLine());
        long timeStamp=Long.parseLong(sc.nextLine());
        int lineCnt=Integer.parseInt(sc.nextLine());
        ArrayList<String> messageLines=new ArrayList<>();
        while(lineCnt-->0){
          String line=sc.nextLine();
          messageLines.add(line);
        }
        controller.downloadMessage(new Message(timeStamp,sendBy,chatId,messageLines));
        break;
      case "RECEIVE_USER_LIST":
        int listLength=Integer.parseInt(sc.nextLine());
        TreeSet<String> userList=new TreeSet<>();
        while(listLength-->0){
          userList.add(sc.nextLine());
        }
        controller.receiveUserList(userList);
        break;
      case "JOIN_CHAT":
        chatId=Long.parseLong(sc.nextLine());
        listLength=Integer.parseInt(sc.nextLine());
        userList=new TreeSet<>();
        while(listLength-->0){
          userList.add(sc.nextLine());
        }
        controller.joinChat(chatId,userList);
        break;
      case "QUIT_CHAT":
        chatId=Long.parseLong(sc.nextLine());
        String userName=sc.nextLine();
        controller.quitChat(chatId,userName);
        break;
    }
  }
}
