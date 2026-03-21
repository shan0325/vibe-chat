package com.shan.chat.adapter.in.websocket.member.session;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class OnlineSessionAdapter implements ManageOnlineSessionPort {

    /** sessionId → MemberInfo */
    private final ConcurrentHashMap<String, MemberInfo> sessions = new ConcurrentHashMap<>();

    /** sessionId → roomId : 해당 세션이 현재 열고 있는 방 */
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    @Override
    public void addSession(String sessionId, MemberInfo memberInfo) {
        sessions.put(sessionId, memberInfo);
    }

    @Override
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        sessionToRoom.remove(sessionId);   // 방 입장 기록도 함께 정리
    }

    @Override
    public Optional<MemberInfo> getMemberBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /** 동일 멤버가 여러 탭으로 접속해도 memberId 기준으로 중복 제거 */
    @Override
    public List<MemberInfo> getAllOnlineMembers() {
        return sessions.values().stream()
                .collect(Collectors.toMap(
                        MemberInfo::getMemberId,
                        m -> m,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public void enterRoom(String sessionId, String roomId) {
        sessionToRoom.put(sessionId, roomId);
    }

    @Override
    public Optional<String> getRoomBySessionId(String sessionId) {
        return Optional.ofNullable(sessionToRoom.get(sessionId));
    }

    /**
     * 현재 해당 방 페이지를 열고 있는 고유 멤버 수.
     * 같은 멤버가 여러 탭으로 같은 방을 열어도 1명으로 센다.
     */
    @Override
    public int countMembersInRoom(String roomId) {
        return (int) sessionToRoom.entrySet().stream()
                .filter(e -> roomId.equals(e.getValue()))
                .map(e -> sessions.get(e.getKey()))
                .filter(Objects::nonNull)
                .map(MemberInfo::getMemberId)
                .distinct()
                .count();
    }

    /**
     * 현재 해당 방 페이지를 열고 있는 MemberInfo 목록.
     * 같은 멤버가 여러 탭으로 같은 방을 열어도 1명으로 센다.
     */
    @Override
    public List<MemberInfo> getMembersInRoom(String roomId) {
        return sessionToRoom.entrySet().stream()
                .filter(e -> roomId.equals(e.getValue()))
                .map(e -> sessions.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        MemberInfo::getMemberId,
                        m -> m,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}
