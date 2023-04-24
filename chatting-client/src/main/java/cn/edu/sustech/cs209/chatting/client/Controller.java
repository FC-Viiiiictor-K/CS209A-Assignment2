package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    @FXML
    ListView<Message> chatContentList;

    String userName;
    private final int port=1453;
    private Socket socket;
    private Scanner sc;
    private PrintWriter pw;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try{
            socket=new Socket("localhost",port);
            sc=new Scanner(socket.getInputStream());
            pw=new PrintWriter(socket.getOutputStream());

            boolean createdUser=false;
            while(!createdUser){
                Dialog<String> dialog = new TextInputDialog();
                dialog.setTitle("Login");
                dialog.setHeaderText(null);
                dialog.setContentText("Username:");

                Optional<String> input = dialog.showAndWait();
                if(!input.isPresent()){
                    Platform.exit();
                    return;
                }
                if (!input.get().isEmpty()) {
                    String tmpName = input.get();
                    pw.println(tmpName);
                    pw.flush();
                    String result=sc.nextLine();
                    if(result.equals("CREATE_USER_SUCCEEDED")){
                        createdUser=true;
                        userName=tmpName;
                    }
                    else{
                        Alert invalidNameAlert=new Alert(Alert.AlertType.WARNING);
                        invalidNameAlert.setTitle("Username Already Exists");
                        invalidNameAlert.setHeaderText(null);
                        invalidNameAlert.setContentText("User \""+tmpName+"\" is already online, please enter another username!");
                        invalidNameAlert.showAndWait();
                    }
                } else {
                    Alert invalidNameAlert=new Alert(Alert.AlertType.WARNING);
                    invalidNameAlert.setTitle("Username Empty");
                    invalidNameAlert.setHeaderText(null);
                    invalidNameAlert.setContentText("Please enter your username!");
                    invalidNameAlert.showAndWait();
                }
            }
            chatContentList.setCellFactory(new MessageCellFactory());
        }
        catch (IOException e){
            Platform.exit();
            e.printStackTrace();
        }
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        pw.println("GET_USER_LIST");
        pw.flush();
        int userCnt=Integer.parseInt(sc.nextLine());
        System.out.println(userCnt);
        while(userCnt-->0){
            String tmp=sc.nextLine();
            if(tmp.equals(userName)){
                continue;
            }
            userSel.getItems().add(tmp);
        }

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
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
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
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
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
                    Label msgLabel = new Label(msg.getData());

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
}
