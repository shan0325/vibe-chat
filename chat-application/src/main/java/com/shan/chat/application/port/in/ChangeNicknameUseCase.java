package com.shan.chat.application.port.in;

import com.shan.chat.application.dto.MemberInfo;

/** 닉네임 변경 유스케이스 */
public interface ChangeNicknameUseCase {
    MemberInfo change(String memberId, String newNickname);
}

