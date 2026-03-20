package com.shan.chat.application.room.port.in;

import com.shan.chat.application.room.dto.RoomDto;

public interface CreateRoomUseCase {
    RoomDto create(String memberId, String roomName);
}

