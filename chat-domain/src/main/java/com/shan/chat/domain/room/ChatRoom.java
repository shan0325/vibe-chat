package com.shan.chat.domain.room;

import com.shan.chat.common.exception.ChatException;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 도메인 모델
 */
@Getter
public class ChatRoom {

    private final String roomId;
    private String roomName;
    private final String creatorId;
    private final LocalDateTime createdAt;
    private boolean active;

    private ChatRoom(String roomId, String roomName, String creatorId, LocalDateTime createdAt, boolean active) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.active = active;
    }

    /** 신규 방 생성 */
    public static ChatRoom create(String roomId, String roomName, String creatorId) {
        if (roomName == null || roomName.isBlank()) {
            throw new ChatException("방 이름은 빈 값일 수 없습니다.");
        }
        if (roomName.length() > 30) {
            throw new ChatException("방 이름은 30자 이하여야 합니다.");
        }
        return new ChatRoom(roomId, roomName, creatorId, LocalDateTime.now(), true);
    }

    /** 영속 계층에서 복원 */
    public static ChatRoom restore(String roomId, String roomName, String creatorId,
                                   LocalDateTime createdAt, boolean active) {
        return new ChatRoom(roomId, roomName, creatorId, createdAt, active);
    }

    public void deactivate() {
        this.active = false;
    }
}

