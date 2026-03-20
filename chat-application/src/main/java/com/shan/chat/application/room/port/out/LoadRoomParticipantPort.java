package com.shan.chat.application.room.port.out;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;

public interface LoadRoomParticipantPort {
    List<String> loadRoomIdsByMemberId(String memberId);
    boolean existsByRoomIdAndMemberId(String roomId, String memberId);
    int countByRoomId(String roomId);
    List<MemberInfo> loadParticipantsWithInfoByRoomId(String roomId);
}

