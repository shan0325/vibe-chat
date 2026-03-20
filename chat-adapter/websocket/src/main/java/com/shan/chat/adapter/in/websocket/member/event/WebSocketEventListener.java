package com.shan.chat.adapter.in.websocket.member.event;

import com.shan.chat.application.member.port.in.ConnectMemberUseCase;
import com.shan.chat.application.member.port.in.DisconnectMemberUseCase;
import com.shan.chat.application.member.port.in.SyncPresenceUseCase;
import com.shan.chat.common.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ConnectMemberUseCase connectMemberUseCase;
    private final DisconnectMemberUseCase disconnectMemberUseCase;
    private final SyncPresenceUseCase syncPresenceUseCase;

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

    /**
     * 클라이언트가 /topic/presence 를 구독하는 순간 현재 온라인 목록을 브로드캐스트한다.
     * addSession() 은 SessionConnectEvent(CONNECT 수신)에서 이미 완료되어 있으므로
     * 이 시점에는 반드시 구독자 자신도 목록에 포함된 상태이다.
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if ("/topic/presence".equals(destination)) {
            log.debug("[WS subscribe] /topic/presence → presence sync");
            syncPresenceUseCase.syncPresence();
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



