package com.shan.chat.application.room.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomDto {
    private String roomId;
    private String roomName;
    private String creatorId;
    private int participantCount;
    private LocalDateTime createdAt;
    private String roomType;   // "GROUP" | "DIRECT"
}

