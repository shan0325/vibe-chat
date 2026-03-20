package com.shan.chat.domain.room;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 방 채팅 메시지 도메인 모델
 */
@Getter
@Builder
public class RoomMessage {

    private Long id;
    private String roomId;
    private String senderId;
    private String senderNickname;
    private String content;
    private MessageType type;
    private LocalDateTime sentAt;

    public enum MessageType {
        TEXT, SYSTEM
    }
}

