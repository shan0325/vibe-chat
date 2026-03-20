package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface LoadRoomPort {
    Optional<ChatRoom> loadById(String roomId);
    List<ChatRoom> loadAllActive();
}

