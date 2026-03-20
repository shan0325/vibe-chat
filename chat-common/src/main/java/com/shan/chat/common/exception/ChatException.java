package com.shan.chat.common.exception;

/**
 * Chat 서비스 공통 최상위 예외
 */
public class ChatException extends RuntimeException {

    public ChatException(String message) {
        super(message);
    }

    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}

