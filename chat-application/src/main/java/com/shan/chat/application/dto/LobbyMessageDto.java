package com.shan.chat.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LobbyMessageDto {

    private String senderId;
    private String senderNickname;
    private String content;
    private String sentAt;       // HH:mm 포맷
    private MessageType type;

    public enum MessageType {
        TEXT, SYSTEM
    }
}

