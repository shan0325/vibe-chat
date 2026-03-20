package com.shan.chat.application.member.port.in;

import com.shan.chat.application.member.dto.MemberInfo;

public interface ChangeNicknameUseCase {
    MemberInfo change(String memberId, String newNickname);
}

