package com.shan.chat.application.port.in;

import com.shan.chat.application.dto.MemberInfo;

/** 신규 멤버 생성 유스케이스 (UUID + 랜덤 닉네임 자동 생성) */
public interface CreateMemberUseCase {
    MemberInfo create();
}

