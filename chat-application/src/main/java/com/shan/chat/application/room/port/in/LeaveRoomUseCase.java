package com.shan.chat.application.room.port.in;

public interface LeaveRoomUseCase {
    void leave(String memberId, String roomId);
}

