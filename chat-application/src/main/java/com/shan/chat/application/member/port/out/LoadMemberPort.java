package com.shan.chat.application.member.port.out;

import com.shan.chat.domain.member.MemberProfile;

import java.util.Optional;

public interface LoadMemberPort {
    Optional<MemberProfile> loadByMemberId(String memberId);
}

