package com.example.safechat.Chat;

import java.util.jar.Manifest;

public class MessageObject {
    String messageId, senderId, message, timestamp;

    public MessageObject(String messageId,String senderId,String message, String timestamp){
        this.messageId=messageId;
        this.senderId=senderId;
        this.message=message;
        this.timestamp=timestamp;
    }
    public String getSenderId(){ return senderId;}
    public String getMessage(){ return message;}
    public String getTimestamp(){ return timestamp;}
}
