package com.shan.chat.application.lobby.port.in;

public interface SendLobbyMessageUseCase {
    void send(String sessionId, String content);
}

