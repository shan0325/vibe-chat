package com.shan.chat.adapter.out.persistence.room.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomJpaEntity {

    @Id
    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "room_name", nullable = false, length = 30)
    private String roomName;

    @Column(name = "creator_id", nullable = false, length = 36)
    private String creatorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "active", nullable = false)
    private boolean active;
}

