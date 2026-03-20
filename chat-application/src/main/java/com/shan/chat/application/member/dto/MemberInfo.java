package com.shan.chat.application.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfo {
    private final String memberId;
    private final String nickname;
    private final boolean randomNickname;
}

