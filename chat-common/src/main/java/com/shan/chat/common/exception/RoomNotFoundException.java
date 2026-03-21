package com.shan.chat.common.exception;

/**
 * 채팅방을 찾을 수 없을 때 발생하는 예외
 */
public class RoomNotFoundException extends ChatException {

    public RoomNotFoundException(String roomId) {
        super("존재하지 않는 방입니다: " + roomId);
    }
}

