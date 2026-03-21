package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.ChatRoom;

import java.util.List;

public interface SearchRoomPort {
    /**
     * 키워드로 활성 GROUP 방을 검색한다.
     * keyword가 null 또는 공백이면 전체 활성 GROUP 방을 반환한다.
     */
    List<ChatRoom> searchGroupRooms(String keyword);
}

