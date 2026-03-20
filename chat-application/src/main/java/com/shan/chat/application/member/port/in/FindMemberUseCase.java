package com.shan.chat.application.member.port.in;

import com.shan.chat.application.member.dto.MemberInfo;

import java.util.Optional;

public interface FindMemberUseCase {
    Optional<MemberInfo> findByMemberId(String memberId);
}

