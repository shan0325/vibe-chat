package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.RoomParticipant;

public interface SaveRoomParticipantPort {
    void save(RoomParticipant participant);
    void deleteByRoomIdAndMemberId(String roomId, String memberId);
}

