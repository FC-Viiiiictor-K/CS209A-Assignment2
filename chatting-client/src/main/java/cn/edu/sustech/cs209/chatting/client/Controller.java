package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chatting;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Controller implements Initializable {
    @FXML
    public ListView<Chatting> chatList;
    @FXML
    public TextArea inputArea;
    @FXML
    public Label currentUsername;
    @FXML
    public Label currentOnlineCnt;
    @FXML
    public Font x3;
    @FXML
    public Color x4;
    @FXML
    ListView<Message> chatContentList;

    String userName;
    private final int port = 1453;
    private final int port2 = 1454;
    private volatile Socket socket, heartbeatSocket;
    private volatile Scanner sc;
    private volatile PrintWriter pw;
    private long currentChatId;
    private int onlineUserCnt = 0;
    private volatile Set<String> tempUserList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("localhost", port);
            heartbeatSocket = new Socket("localhost", port2);
            sc = new Scanner(socket.getInputStream());
            pw = new PrintWriter(socket.getOutputStream());

            boolean createdUser=false;
            while (!createdUser) {
                Dialog<String> dialog = new TextInputDialog();
                dialog.setTitle("Login");
                dialog.setHeaderText(null);
                dialog.setContentText("Username:");

                Optional<String> input = dialog.showAndWait();
                if (!input.isPresent()) {
                    Platform.exit();
                    return;
                }
                if (!input.get().isEmpty()) {
                    String tmpName = input.get();
                    pw.println(tmpName);
                    pw.flush();
                    String result = sc.nextLine();
                    if (result.equals("CREATE_USER_SUCCEEDED")) {
                        createdUser = true;
                        userName = tmpName;
                    } else {
                        Alert invalidNameAlert = new Alert(Alert.AlertType.WARNING);
                        invalidNameAlert.setTitle("Username already exists");
                        invalidNameAlert.setHeaderText(null);
                        invalidNameAlert.setContentText("User \""
                            + tmpName
                            + "\" is already online, please enter another username!");
                        invalidNameAlert.showAndWait();
                    }
                } else {
                    Alert invalidNameAlert = new Alert(Alert.AlertType.WARNING);
                    invalidNameAlert.setTitle("Username empty");
                    invalidNameAlert.setHeaderText(null);
                    invalidNameAlert.setContentText("Please enter your username!");
                    invalidNameAlert.showAndWait();
                }
            }

            pw.println("REFRESH_ONLINE_CNT");
            pw.flush();
            String inform = sc.nextLine();
            if(inform.equals("SET_ONLINE_CNT")){
                onlineUserCnt=Integer.parseInt(sc.nextLine());
                currentOnlineCnt.setText("Online: "+onlineUserCnt);
            }
            currentUsername.setText(currentUsername.getText().replace("USERNAME",userName));
            chatContentList.setCellFactory(new MessageCellFactory());
            chatList.setCellFactory(new ChatCellFactory());

            Thread thread=new Thread(new ReceiveMessageThread(this,socket,sc,pw));
            thread.start();
            Thread thread2=new Thread(new HeartbeatThread(
              this,
              new Scanner(heartbeatSocket.getInputStream()),
              new PrintWriter(heartbeatSocket.getOutputStream())));
            thread2.start();
        }
        catch (IOException e){
            exitDueToServer();
        }
    }
    
    public void exitDueToServer(){
        Platform.runLater(() -> {
            Alert invalidNameAlert = new Alert(Alert.AlertType.INFORMATION);
            invalidNameAlert.setTitle("Server offline");
            invalidNameAlert.setHeaderText(null);
            invalidNameAlert.setContentText("Sorry, the server is offline.");
            invalidNameAlert.showAndWait();
            Platform.exit();
        });
    }

    public void setOnlineCnt(int onlineCnt){
        onlineUserCnt=onlineCnt;
        Platform.runLater(() -> currentOnlineCnt.setText("Online: "+onlineUserCnt));
    }

    public void downloadMessage(Message message){
        for(Chatting chatting : chatList.getItems()){
            if(chatting.getChatId()==message.getSendTo()){
                chatting.addMessage(message);
                if(!message.getSentBy().equals(userName)){
                    chatting.setHasNewMessage(true);
                }
                Platform.runLater(() -> {
                    if(chatting.getChatId()==currentChatId){
                        displayChatContent(chatting);
                    }
                    else{
                        chatList.refresh();
                    }
                });
                break;
            }
        }
    }

    public void receiveUserList(Set<String> userList){
        tempUserList=userList;
    }

    public void joinChat(long chatId,TreeSet<String> userList){
        Platform.runLater(() -> {
            Chatting newChatting =new Chatting(chatId,userList);
            currentChatId= newChatting.getChatId();
            displayChatContent(newChatting);
            chatList.getItems().add(newChatting);
        });
    }

    public void quitChat(long chatId,String userName){
        for(Chatting chatting : chatList.getItems()){
            if(chatting.getChatId()==chatId){
                chatting.deleteUser(userName);
                Platform.runLater(() -> chatList.refresh());
                break;
            }
        }
    }

    private void displayChatContent(Chatting chatting){
        for(Chatting chatting1:chatList.getItems()){
            if(chatting1.getChatId()==chatting.getChatId()){
                chatting1.setHasNewMessage(false);
            }
        }
        chatContentList.getItems().clear();
        chatContentList.getItems().addAll(chatting.getMessageList());
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        pw.println("GET_USER_LIST");
        pw.flush();
        while(tempUserList==null);
        tempUserList.remove(userName);
        userSel.getItems().addAll(tempUserList);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.setTitle("Select a user to start a private chat");
        stage.showAndWait();


        String selectedUser=user.get();
        if(selectedUser!=null&&!selectedUser.isEmpty()){
            boolean hasRelatedChat=false;
            long relatedChatId=0;
            for(Chatting chatting : chatList.getItems()){
                if(chatting.getInitUserList().equals(new TreeSet<>(Arrays.asList(userName, selectedUser)))){
                    hasRelatedChat=true;
                    relatedChatId= chatting.getChatId();
                    break;
                }
            }
            if(hasRelatedChat){
                for(Chatting chatting : chatList.getItems()){
                    if(chatting.getChatId()==relatedChatId){
                        chatting.setHasNewMessage(true);
                        chatList.refresh();
                        displayChatContent(chatting);
                        break;
                    }
                }
            }
            else{
                pw.print("CREATE_CHAT\n2\n"+selectedUser+"\n"+userName+"\n");
                pw.flush();
            }
        }
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        Stage stage = new Stage();

        pw.println("GET_USER_LIST");
        pw.flush();
        while(tempUserList==null);
        tempUserList.remove(userName);
        AtomicReference<TreeSet<String>> result=new AtomicReference<>();
        ArrayList<CheckBox> boxes=new ArrayList<>();
        for(String s:tempUserList){
            boxes.add(new CheckBox(s));
        }

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            result.set(new TreeSet<>());
            for(CheckBox box:boxes){
                if(box.isSelected()){
                    result.get().add(box.getText());
                }
            }
            stage.close();
        });

        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        for(CheckBox box1:boxes){
            box.getChildren().add(box1);
        }
        box.getChildren().add(okBtn);
        stage.setScene(new Scene(box));
        stage.setTitle("Select users to start a group chat");
        stage.showAndWait();

        if(result.get()==null){
            return;
        }

        if(result.get().size()>=2){
            result.get().add(userName);
            boolean hasRelatedChat=false;
            long relatedChatId=0;
            for(Chatting chatting : chatList.getItems()){
                if(chatting.getInitUserList().equals(result.get())){
                    hasRelatedChat=true;
                    relatedChatId= chatting.getChatId();
                    break;
                }
            }
            if(hasRelatedChat){
                for(Chatting chatting : chatList.getItems()){
                    if(chatting.getChatId()==relatedChatId){
                        chatting.setHasNewMessage(true);
                        chatList.refresh();
                        displayChatContent(chatting);
                        break;
                    }
                }
            }
            else{
                pw.print("CREATE_CHAT\n"+ result.get().size()+"\n");
                for(String s: result.get()){
                    pw.println(s);
                }
                pw.flush();
            }
        }
        else{
            Alert invalidNameAlert = new Alert(Alert.AlertType.INFORMATION);
            invalidNameAlert.setTitle("Not enough users");
            invalidNameAlert.setHeaderText(null);
            invalidNameAlert.setContentText("Please select at least 2 users to start a group chat.");
            invalidNameAlert.showAndWait();
        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        String inputMessage=inputArea.getText();
        inputArea.setText("");
        if(inputMessage.isEmpty()){
            Alert invalidNameAlert = new Alert(Alert.AlertType.INFORMATION);
            invalidNameAlert.setTitle("Empty message");
            invalidNameAlert.setHeaderText(null);
            invalidNameAlert.setContentText("Message cannot be empty.");
            invalidNameAlert.showAndWait();
        }
        else{
            String[] messageLines=inputMessage.split("\n");
            //chatContentList.getItems().add(new Message(System.currentTimeMillis(),userName, 1L,new ArrayList<>(Arrays.asList(messageLines))));
            pw.print("UPLOAD_MESSAGE\n"+currentChatId+"\n"+System.currentTimeMillis()+"\n"+messageLines.length+"\n");
            for(String line:messageLines){
                pw.println(line);
            }
            pw.flush();
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getMessageString());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (userName.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    private class ChatCellFactory implements Callback<ListView<Chatting>, ListCell<Chatting>> {
        @Override
        public ListCell<Chatting> call(ListView<Chatting> param) {
            return new ListCell<Chatting>() {
                @Override
                public void updateItem(Chatting chatting, boolean empty) {
                    super.updateItem(chatting, empty);
                    if (empty || Objects.isNull(chatting)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    StringBuilder labelName=new StringBuilder();
                    if(chatting.getInitUserList().size()==2){
                        TreeSet<String> tmp=new TreeSet<>(chatting.getInitUserList());
                        tmp.remove(userName);
                        for(String s:tmp){
                            labelName.append(s);
                        }
                    }
                    else{
                        labelName.append(chatting.getChatName()).append("\n").append("    Online:");
                        for(String userName: chatting.getUserList()){
                            labelName.append("\n    ").append(userName);
                        }
                    }
                    Label chatLabel = new Label(labelName.toString());

                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(chatLabel);
                    chatLabel.setPadding(new Insets(0, 0, 0, 20));
                    if(chatting.isHasNewMessage()){
                        chatLabel.setBackground(new Background(new BackgroundFill(Color.rgb(255,200,100), CornerRadii.EMPTY,Insets.EMPTY)));
                    }
                    if(chatting.getUserList().size()<=1){
                        chatLabel.setBackground(new Background(new BackgroundFill(Color.rgb(255,0,0), CornerRadii.EMPTY,Insets.EMPTY)));
                    }

                    setOnMouseClicked(event -> {
                        currentChatId= chatting.getChatId();
                        chatting.setHasNewMessage(false);
                        displayChatContent(chatting);
                        if(chatting.getUserList().size()>=2){
                            chatLabel.setBackground(Background.EMPTY);
                        }
                        else{
                            chatLabel.setBackground(new Background(new BackgroundFill(Color.rgb(255,0,0), CornerRadii.EMPTY,Insets.EMPTY)));
                        }
                    });

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
