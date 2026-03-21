package com.shan.chat.application.member.port.out;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;
import java.util.Optional;

public interface ManageOnlineSessionPort {
    void addSession(String sessionId, MemberInfo memberInfo);
    void removeSession(String sessionId);
    Optional<MemberInfo> getMemberBySessionId(String sessionId);
    List<MemberInfo> getAllOnlineMembers();

    /** 해당 세션이 특정 방 페이지를 열었음을 기록한다. */
    void enterRoom(String sessionId, String roomId);

    /** 세션 제거 전 현재 방 roomId를 반환한다. */
    Optional<String> getRoomBySessionId(String sessionId);

    /** 현재 해당 방 페이지를 열고 있는 고유 멤버 수를 반환한다. */
    int countMembersInRoom(String roomId);

    /** 현재 해당 방 페이지를 열고 있는 MemberInfo 목록을 반환한다 (memberId 기준 중복 제거). */
    List<MemberInfo> getMembersInRoom(String roomId);
}
