package com.shan.chat.adapter.in.websocket.lobby.controller;

import com.shan.chat.adapter.in.websocket.lobby.dto.ChatMessageRequest;
import com.shan.chat.application.lobby.port.in.SendLobbyMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LobbyStompController {

    private final SendLobbyMessageUseCase sendLobbyMessageUseCase;

    @MessageMapping("/lobby/message")
    public void handleLobbyMessage(@Payload ChatMessageRequest request,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String content   = request.getContent();
        if (sessionId != null && content != null && !content.isBlank()) {
            sendLobbyMessageUseCase.send(sessionId, content.trim());
        }
    }
}

