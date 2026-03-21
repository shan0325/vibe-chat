package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface FindDirectRoomPort {
    /** 두 멤버 간의 1:1 방을 조회한다 (순서 무관). */
    Optional<ChatRoom> findDirectRoom(String member1Id, String member2Id);

    /** 특정 멤버가 참여 중인 모든 1:1 방 목록 */
    List<ChatRoom> findDirectRoomsByMemberId(String memberId);
}

