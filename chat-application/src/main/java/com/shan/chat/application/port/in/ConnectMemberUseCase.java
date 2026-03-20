package com.shan.chat.application.port.in;

/** WebSocket 세션 연결 시 멤버 입장 처리 유스케이스 */
public interface ConnectMemberUseCase {
    void connect(String sessionId, String memberId);
}

