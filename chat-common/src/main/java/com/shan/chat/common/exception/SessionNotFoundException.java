package com.shan.chat.common.exception;

/**
 * 세션을 찾을 수 없을 때 발생하는 예외
 */
public class SessionNotFoundException extends ChatException {

    public SessionNotFoundException(String sessionId) {
        super("세션을 찾을 수 없습니다: " + sessionId);
    }
}

