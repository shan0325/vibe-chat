package com.shan.chat.adapter.in.websocket.room.controller;

import com.shan.chat.adapter.in.websocket.room.dto.RoomChatMessageRequest;
import com.shan.chat.application.room.port.in.SendRoomMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomStompController {

    private final SendRoomMessageUseCase sendRoomMessageUseCase;

    @MessageMapping("/room/{roomId}/message")
    public void handleRoomMessage(@Payload RoomChatMessageRequest request,
                                  @DestinationVariable String roomId,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String content   = request.getContent();
        if (sessionId != null && content != null && !content.isBlank()) {
            sendRoomMessageUseCase.send(sessionId, roomId, content.trim());
        }
    }
}

