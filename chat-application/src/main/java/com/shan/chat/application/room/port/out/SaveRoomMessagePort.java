package com.shan.chat.application.room.port.out;

import com.shan.chat.domain.room.RoomMessage;

public interface SaveRoomMessagePort {
    void save(RoomMessage message);
}

