package com.shan.chat.domain.message;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessage {

    private Long id;
    private String senderId;
    private String senderNickname;
    private String content;
    private MessageType type;
    private LocalDateTime sentAt;

    public enum MessageType {
        TEXT, SYSTEM
    }
}

