package com.shan.chat.application.room.port.in;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;

public interface GetRoomParticipantsUseCase {
    List<MemberInfo> getParticipants(String roomId);
}

