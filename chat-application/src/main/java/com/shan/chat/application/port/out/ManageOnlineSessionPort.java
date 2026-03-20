package com.shan.chat.application.port.out;

import com.shan.chat.application.dto.MemberInfo;

import java.util.List;
import java.util.Optional;

/** 인메모리 온라인 세션 관리 아웃바운드 포트 */
public interface ManageOnlineSessionPort {
    void addSession(String sessionId, MemberInfo memberInfo);
    void removeSession(String sessionId);
    Optional<MemberInfo> getMemberBySessionId(String sessionId);
    List<MemberInfo> getAllOnlineMembers();
}

