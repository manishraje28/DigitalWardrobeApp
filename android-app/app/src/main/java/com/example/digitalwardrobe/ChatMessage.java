package com.example.digitalwardrobe;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    public String message;
    public int senderType;

    public ChatMessage(String message, int senderType) {
        this.message = message;
        this.senderType = senderType;
    }
}
