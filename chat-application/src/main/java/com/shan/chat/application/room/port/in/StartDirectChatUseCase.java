package com.shan.chat.application.room.port.in;

import com.shan.chat.application.room.dto.RoomDto;

import java.util.List;

public interface StartDirectChatUseCase {
    /** 두 사용자 간 1:1 방을 찾거나 없으면 새로 생성한다. */
    RoomDto startDirectChat(String requesterId, String targetMemberId);

    /** 내가 참여 중인 1:1 방 목록 */
    List<RoomDto> getMyDirectRooms(String memberId);
}

