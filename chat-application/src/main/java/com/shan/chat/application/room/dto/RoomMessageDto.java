package com.shan.chat.application.room.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomMessageDto {
    private String roomId;
    private String senderId;
    private String senderNickname;
    private String content;
    private String sentAt;
    private MessageType type;

    public enum MessageType {
        TEXT, SYSTEM
    }
}

