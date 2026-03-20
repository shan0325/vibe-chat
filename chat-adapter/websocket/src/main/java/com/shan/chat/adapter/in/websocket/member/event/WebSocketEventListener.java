package com.shan.chat.adapter.in.websocket.member.event;

import com.shan.chat.application.member.port.in.ConnectMemberUseCase;
import com.shan.chat.application.member.port.in.DisconnectMemberUseCase;
import com.shan.chat.common.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ConnectMemberUseCase connectMemberUseCase;
    private final DisconnectMemberUseCase disconnectMemberUseCase;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String memberId  = accessor.getFirstNativeHeader("memberId");

        log.debug("[WS connect] sessionId={}, memberId={}", sessionId, memberId);

        if (sessionId != null && memberId != null && !memberId.isBlank()) {
            try {
                connectMemberUseCase.connect(sessionId, memberId);
            } catch (ChatException e) {
                log.warn("[WS connect] 처리 실패: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.debug("[WS disconnect] sessionId={}", sessionId);

        if (sessionId != null) {
            disconnectMemberUseCase.disconnect(sessionId);
        }
    }
}

