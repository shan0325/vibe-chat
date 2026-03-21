package com.shan.chat.application.room.port.out;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.dto.RoomMessageDto;

import java.util.List;

public interface BroadcastRoomPort {
    void broadcastRoomMessage(String roomId, RoomMessageDto message);
    void broadcastRoomParticipants(String roomId, List<MemberInfo> participants);
    void broadcastRoomList(List<RoomDto> rooms);

    /** 특정 멤버의 1:1 대화 목록을 실시간으로 갱신한다. */
    void notifyDirectChatUpdate(String memberId, List<RoomDto> directRooms);
}

