package com.shan.chat.application.member.port.out;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;
import java.util.Optional;

public interface ManageOnlineSessionPort {
    void addSession(String sessionId, MemberInfo memberInfo);
    void removeSession(String sessionId);
    Optional<MemberInfo> getMemberBySessionId(String sessionId);
    List<MemberInfo> getAllOnlineMembers();
}

