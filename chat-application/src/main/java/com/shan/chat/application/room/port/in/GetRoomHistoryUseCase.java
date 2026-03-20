package com.shan.chat.application.room.port.in;

import com.shan.chat.application.room.dto.RoomMessageDto;

import java.util.List;

public interface GetRoomHistoryUseCase {
    List<RoomMessageDto> getRecentHistory(String roomId, int limit);
}

