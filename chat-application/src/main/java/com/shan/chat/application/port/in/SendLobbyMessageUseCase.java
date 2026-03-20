package com.shan.chat.application.port.in;

/** 로비 메시지 전송 유스케이스 */
public interface SendLobbyMessageUseCase {
    void send(String sessionId, String content);
}

