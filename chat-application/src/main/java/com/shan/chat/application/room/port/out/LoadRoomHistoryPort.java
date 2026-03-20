package com.shan.chat.application.room.port.out;

import com.shan.chat.application.room.dto.RoomMessageDto;

import java.util.List;

public interface LoadRoomHistoryPort {
    List<RoomMessageDto> loadRecent(String roomId, int limit);
}

