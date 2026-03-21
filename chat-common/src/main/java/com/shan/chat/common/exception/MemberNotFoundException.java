package com.shan.chat.common.exception;

/**
 * 멤버를 찾을 수 없을 때 발생하는 예외
 */
public class MemberNotFoundException extends ChatException {

    public MemberNotFoundException(String memberId) {
        super("사용자를 찾을 수 없습니다: " + memberId);
    }
}

