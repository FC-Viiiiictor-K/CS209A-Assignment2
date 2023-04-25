package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;

import java.util.*;

/**
 * Console -> Server:
 * REFRESH_ONLINE_CNT // refresh onlineCnt on all consoles
 * CREATE_USER userName // create the user of the console, this will only automatically happen during opening a console
 *     If userName already exists, the server should return a "CREATE_USER_REJECTED" to the console
 * DELETE_USER userName // delete the user of the console, this will only automatically happen during closing a console
 *     When userName is deleted, the server will automatically execute "QUIT_CHAT userName [all the chats userName is in]"
 * CREATE_CHAT listLength [userList] // create a chat between two or more people
 *     If such chat already exists, the server should return a "CREATE_CHAT_REJECTED" to the console
 *     When this was executed, the server will return "JOIN_CHAT chat" to other users
 * GET_USER_LIST // get a list of current user
 * UPLOAD_MESSAGE chatId timeStamp lineCnt [messageLines] // a user sends message to a chat
 *     When this was executed, the server will return "DOWNLOAD_MESSAGE sendBy chatId message timeStamp" to other users
 * --------------------
 * Server -> Console:
 * SET_ONLINE_CNT onlineCnt // set the counter of number of online users
 * CREATE_USER_REJECTED // the user already exists
 * DOWNLOAD_MESSAGE sendBy chatId timeStamp lineCnt [messageLines] // get the message that other user send
 * RECEIVE_USER_LIST listLength [userList] // get the user list
 * JOIN_CHAT chatId listLength [userList] // join a new chat
 * QUIT_CHAT chatId userName // userName quited a chat which the user is in
 */
public class ServerController {
    private static volatile ArrayList<ChatService> serviceList;
    private static volatile ArrayList<Chatting> chattingList;
    private static volatile Random random;
    public ServerController(){
        serviceList=new ArrayList<>();
        chattingList =new ArrayList<>();
        random=new Random();
    }

    public synchronized void addChatService(ChatService service){
        serviceList.add(service);
    }

    public synchronized void removeChatService(ChatService service){
        serviceList.remove(service);
        quitChat(service.getUserName());
    }

    public void refreshOnlineCnt(){
        for(ChatService service:serviceList){
            service.setOnlineCnt(serviceList.size());
        }
    }

    public synchronized boolean existsUser(String userName){
        //System.out.println(serviceList.size());
        for(ChatService service:serviceList){
            //System.out.print(service.getUserName()+";");
            if(service.getUserName().equals(userName)){
                //System.out.println();
                return true;
            }
        }
        //System.out.println();
        return false;
    }

    public synchronized void createChat(TreeSet<String> users){
        Chatting newChatting =new Chatting(random.nextLong(),users);
        chattingList.add(newChatting);
        for(ChatService service:serviceList){
            if(users.contains(service.getUserName())){
                service.joinChat(newChatting);
            }
        }
    }

    public Set<String> getUserList(){
        Set<String> userList=new TreeSet<>();
        for(ChatService service:serviceList){
            userList.add(service.getUserName());
        }
        return userList;
    }

    public void uploadMessage(Message message){
        for(Chatting chatting : chattingList){
            if(chatting.getChatId()==message.getSendTo()){
                for(ChatService service:serviceList){
                    if(chatting.getUserList().contains(service.getUserName())){
                        service.downloadMessage(message);
                    }
                }
                return;
            }
        }
    }

    public synchronized void quitChat(String userName){
        for(Chatting chatting : chattingList){
            if(chatting.getUserList().contains(userName)){
                chatting.deleteUser(userName);
                for(ChatService service:serviceList){
                    if(chatting.getUserList().contains(service.getUserName())){
                        System.out.println(chatting.getChatName()+","+userName);
                        service.quitChat(chatting.getChatId(), userName);
                    }
                }
            }
        }
        chattingList.removeIf(chatting -> chatting.getUserList().size() == 0);
    }
}
