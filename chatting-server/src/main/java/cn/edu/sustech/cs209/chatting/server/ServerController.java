package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;

import java.util.*;

/**
 * Console -> Server:
 * CREATE_USER userName // create the user of the console, this will only automatically happen during opening a console
 *     If userName already exists, the server should return a "CREATE_USER_REJECTED" to the console
 * DELETE_USER userName // delete the user of the console, this will only automatically happen during closing a console
 *     When userName is deleted, the server will automatically execute "QUIT_CHAT userName [all the chats userName is in]"
 * CREATE_CHAT listLength [userList] // create a chat between two or more people
 *     If such chat already exists, the server should return a "CREATE_CHAT_REJECTED" to the console
 *     When this was executed, the server will return "JOIN_CHAT chat" to other users
 * GET_USER_LIST // get a list of current user
 * UPLOAD_MESSAGE chatId timeStamp message // a user sends message to a chat
 *     When this was executed, the server will return "DOWNLOAD_MESSAGE sendBy chatId message timeStamp" to other users
 * QUIT_CHAT chatId // user quits the chat
 *     When this was executed, the server will return "QUIT_CHAT chat userName" to other users in the chat
 *     If there are no user in a chat, the chat will be deleted
 * --------------------
 * Server -> Console:
 * CREATE_USER_REJECTED // the user already exists
 * DOWNLOAD_MESSAGE sendBy chatId timeStamp message // get the message that other user send
 * JOIN_CHAT chatId listLength [userList] // join a new chat
 * QUIT_CHAT chatId userName // userName quited a chat which the user is in
 */
public class ServerController {
    private static volatile ArrayList<ChatService> serviceList;
    private static volatile ArrayList<Chat> chatList;
    private static volatile Random random;
    public ServerController(){
        serviceList=new ArrayList<>();
        chatList=new ArrayList<>();
        random=new Random();
    }

    public void addChatService(ChatService service){
        serviceList.add(service);
    }

    public void removeChatService(ChatService service){
        serviceList.remove(service);
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

    public synchronized void createChat(Set<String> users){
        Chat newChat=new Chat(random.nextLong(),users);
        chatList.add(newChat);
        for(ChatService service:serviceList){
            if(users.contains(service.getUserName())){
                service.joinChat(newChat);
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
        for(Chat chat:chatList){
            if(chat.getChatId()==message.getSendTo()){
                for(ChatService service:serviceList){
                    if(chat.getUserList().contains(service.getUserName())){
                        service.downloadMessage(message);
                    }
                }
                return;
            }
        }
    }

    public void quitChat(long chatId,String userName){
        for(Chat chat:chatList){
            if(chat.getChatId()==chatId){
                chat.deleteUser(userName);
                for(ChatService service:serviceList){
                    if(chat.getUserList().contains(service.getUserName())){
                        service.quitChat(chatId,userName);
                    }
                }
                if(chat.getUserList().size()==0){
                    chatList.remove(chat);
                }
            }
        }
    }
}
