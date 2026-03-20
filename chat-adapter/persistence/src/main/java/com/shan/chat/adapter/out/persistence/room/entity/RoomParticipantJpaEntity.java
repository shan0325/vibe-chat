package com.shan.chat.adapter.out.persistence.room.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomParticipantJpaEntity {

    @EmbeddedId
    private RoomParticipantId id;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}

