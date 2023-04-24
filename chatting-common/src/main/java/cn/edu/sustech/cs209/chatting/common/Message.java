package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;

public class Message {

    private Long timestamp;

    private String sentBy;

    private Long sendTo;

    private String data;

    public Message(Long timestamp, String sentBy, Long sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
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

    public String getData() {
        return data;
    }
}
