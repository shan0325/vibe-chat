package com.shan.chat.application.port.out;

import com.shan.chat.domain.member.MemberProfile;

/** 멤버 저장 아웃바운드 포트 */
public interface SaveMemberPort {
    void save(MemberProfile memberProfile);
}

