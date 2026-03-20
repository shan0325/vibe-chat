package com.shan.chat.adapter.in.websocket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 클라이언트 → 서버 메시지 요청 DTO */
@Data
@NoArgsConstructor
public class ChatMessageRequest {
    private String content;
}

