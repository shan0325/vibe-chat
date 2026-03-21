package com.shan.chat.application.room.port.in;

import com.shan.chat.application.room.dto.RoomDto;

import java.util.List;

public interface SearchRoomUseCase {
    /**
     * 방 이름 키워드로 GROUP 방을 검색한다.
     * keyword가 null 또는 공백이면 전체 활성 GROUP 방을 반환한다.
     */
    List<RoomDto> searchRooms(String keyword);
}

