package com.shan.chat.domain.room;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 방 참여자 도메인 모델
 */
@Getter
public class RoomParticipant {

    private final String roomId;
    private final String memberId;
    private final LocalDateTime joinedAt;

    private RoomParticipant(String roomId, String memberId, LocalDateTime joinedAt) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.joinedAt = joinedAt;
    }

    public static RoomParticipant join(String roomId, String memberId) {
        return new RoomParticipant(roomId, memberId, LocalDateTime.now());
    }

    public static RoomParticipant restore(String roomId, String memberId, LocalDateTime joinedAt) {
        return new RoomParticipant(roomId, memberId, joinedAt);
    }
}

