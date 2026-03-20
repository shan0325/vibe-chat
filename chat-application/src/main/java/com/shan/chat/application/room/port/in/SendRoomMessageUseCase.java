package com.shan.chat.application.room.port.in;

public interface SendRoomMessageUseCase {
    void send(String sessionId, String roomId, String content);
}

