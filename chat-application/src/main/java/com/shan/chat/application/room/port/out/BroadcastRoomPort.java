package com.shan.chat.application.room.port.out;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.dto.RoomMessageDto;

import java.util.List;

public interface BroadcastRoomPort {
    void broadcastRoomMessage(String roomId, RoomMessageDto message);
    void broadcastRoomParticipants(String roomId, List<MemberInfo> participants);
    void broadcastRoomList(List<RoomDto> rooms);
}

