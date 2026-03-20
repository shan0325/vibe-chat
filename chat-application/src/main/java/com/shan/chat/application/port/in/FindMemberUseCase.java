package com.shan.chat.application.port.in;

import com.shan.chat.application.dto.MemberInfo;

import java.util.Optional;

/** 멤버 조회 유스케이스 */
public interface FindMemberUseCase {
    Optional<MemberInfo> findByMemberId(String memberId);
}

