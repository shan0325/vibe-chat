package com.shan.chat.application.room.port.in;

import com.shan.chat.application.room.dto.RoomDto;

import java.util.List;
import java.util.Optional;

public interface GetRoomListUseCase {
    List<RoomDto> getRoomList();
    Optional<RoomDto> getRoomById(String roomId);
}

