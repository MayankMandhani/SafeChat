package com.example.safechat.Chat;

public class ChatObject {
    private String chatId;

    public ChatObject(String chatId){
        this.chatId=chatId;
    }

    public String getChatId(){
        return chatId;
    }
}
