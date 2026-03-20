package com.shan.chat.application.port.out;

import com.shan.chat.domain.member.MemberProfile;

import java.util.Optional;

/** 멤버 조회 아웃바운드 포트 */
public interface LoadMemberPort {
    Optional<MemberProfile> loadByMemberId(String memberId);
}

