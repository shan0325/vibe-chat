package com.shan.chat.application.member.port.in;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;

public interface GetOnlineMembersUseCase {
    List<MemberInfo> getOnlineMembers();
}

