package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.ChatRoom;

public interface SaveRoomPort {
    void save(ChatRoom room);
}

