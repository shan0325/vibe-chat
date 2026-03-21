package com.shan.chat.application.member.port.in;

public interface DisconnectMemberUseCase {
    void disconnect(String sessionId);

    /**
     * 세션을 제거하고 해당 세션의 memberId를 반환한다.
     * 세션이 없으면 null을 반환한다.
     */
    String getAndDisconnect(String sessionId);
}
