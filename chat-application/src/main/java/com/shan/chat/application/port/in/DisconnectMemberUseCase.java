package com.shan.chat.application.port.in;

/** WebSocket 세션 종료 시 멤버 퇴장 처리 유스케이스 */
public interface DisconnectMemberUseCase {
    void disconnect(String sessionId);
}

