package com.shan.chat.adapter.in.websocket.member.session;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

