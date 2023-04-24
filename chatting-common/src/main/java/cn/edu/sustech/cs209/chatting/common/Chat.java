package cn.edu.sustech.cs209.chatting.common;

import java.util.*;

public class Chat {
    private final long chatId;
    private Set<String> userList;
    public Chat(long chatId, Set<String> userList){
        this.chatId=chatId;
        this.userList=userList;
    }

    public long getChatId(){
        return chatId;
    }

    public Set<String> getUserList(){
        return userList;
    }

    public void deleteUser(String userName){
        userList.remove(userName);
    }
}
