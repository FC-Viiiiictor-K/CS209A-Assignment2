package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;

public class Message {
  
  private Long timestamp;
  
  private String sentBy;
  
  private Long sendTo;
  
  private ArrayList<String> messageLines;
  
  public Message(Long timestamp, String sentBy, Long sendTo, ArrayList<String> messageLines) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendTo;
    this.messageLines = messageLines;
  }
  
  public Long getTimestamp() {
    return timestamp;
  }
  
  public String getSentBy() {
    return sentBy;
  }
  
  public Long getSendTo() {
    return sendTo;
  }
  
  public ArrayList<String> getMessageLines() {
    return messageLines;
  }
  
  public String getMessageString(){
    StringBuilder sb=new StringBuilder();
    for(String line:messageLines){
      sb.append(line).append("\n");
    }
    sb.delete(sb.length()-1,sb.length());
    return sb.toString();
  }
}
