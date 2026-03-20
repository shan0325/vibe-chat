package com.shan.chat.application.member.port.out;

import com.shan.chat.domain.member.MemberProfile;

public interface SaveMemberPort {
    void save(MemberProfile memberProfile);
}

