package com.shan.chat.adapter.in.websocket.lobby.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageRequest {
    private String content;
}

