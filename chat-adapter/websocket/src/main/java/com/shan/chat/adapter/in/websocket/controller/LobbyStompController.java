package com.shan.chat.adapter.in.websocket.controller;

import com.shan.chat.adapter.in.websocket.dto.ChatMessageRequest;
import com.shan.chat.application.port.in.SendLobbyMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LobbyStompController {

    private final SendLobbyMessageUseCase sendLobbyMessageUseCase;

    /**
     * 클라이언트 발행: /pub/lobby/message
     * 세션 ID를 기반으로 발신자를 특정해 로비 전체에 브로드캐스트한다.
     */
    @MessageMapping("/lobby/message")
    public void handleLobbyMessage(@Payload ChatMessageRequest request,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String content = request.getContent();
        if (sessionId != null && content != null && !content.isBlank()) {
            sendLobbyMessageUseCase.send(sessionId, content.trim());
        }
    }
}

