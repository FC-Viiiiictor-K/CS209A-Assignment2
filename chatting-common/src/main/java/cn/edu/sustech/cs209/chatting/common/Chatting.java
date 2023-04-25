package cn.edu.sustech.cs209.chatting.common;

import java.util.*;

public class Chatting {
  private final long chatId;
  private final String chatName;
  private final TreeSet<String> userList,initUserList;
  private final ArrayList<Message> messageList;
  private boolean hasNewMessage;
  public Chatting(long chatId, TreeSet<String> userList){
    this.chatId=chatId;
    this.userList=userList;
    initUserList=new TreeSet<>(userList);
    StringBuilder tmp=new StringBuilder();
    int nameCnt=0;
    for(String user:userList){
      tmp.append(user).append(",");
      nameCnt++;
      if(nameCnt==3){
        break;
      }
    }
    if(userList.size()>3){
      tmp.append("...");
    }
    else{
      tmp.delete(tmp.length()-1,tmp.length());
    }
    chatName=tmp.toString();
    messageList=new ArrayList<>();
    hasNewMessage=true;
  }
  
  public long getChatId(){
    return chatId;
  }
  
  public String getChatName(){
    return chatName;
  }
  
  public TreeSet<String> getUserList(){
    return userList;
  }
  
  public TreeSet<String> getInitUserList(){
    return initUserList;
  }
  
  public boolean isHasNewMessage() {
    return hasNewMessage;
  }
  
  public ArrayList<Message> getMessageList(){
    return messageList;
  }
  
  public void setHasNewMessage(boolean hasNewMessage){
    this.hasNewMessage=hasNewMessage;
  }
  
  public void deleteUser(String userName){
    userList.remove(userName);
  }
  
  public void addMessage(Message message){
    messageList.add(message);
    messageList.sort(Comparator.comparingLong(Message::getTimestamp));
  }
}
