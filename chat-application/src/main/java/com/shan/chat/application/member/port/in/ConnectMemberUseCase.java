package com.shan.chat.application.member.port.in;

public interface ConnectMemberUseCase {
    void connect(String sessionId, String memberId);
}

