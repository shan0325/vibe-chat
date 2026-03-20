package com.shan.chat.adapter.out.persistence.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipantId implements Serializable {

    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "member_id", length = 36)
    private String memberId;
}

