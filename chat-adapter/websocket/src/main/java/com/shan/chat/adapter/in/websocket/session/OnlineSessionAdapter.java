package com.shan.chat.adapter.in.websocket.session;

import com.shan.chat.application.dto.MemberInfo;
import com.shan.chat.application.port.out.ManageOnlineSessionPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 인메모리 WebSocket 세션 관리 어댑터 */
@Component
public class OnlineSessionAdapter implements ManageOnlineSessionPort {

    private final ConcurrentHashMap<String, MemberInfo> sessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(String sessionId, MemberInfo memberInfo) {
        sessions.put(sessionId, memberInfo);
    }

    @Override
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public Optional<MemberInfo> getMemberBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<MemberInfo> getAllOnlineMembers() {
        return List.copyOf(sessions.values());
    }
}

