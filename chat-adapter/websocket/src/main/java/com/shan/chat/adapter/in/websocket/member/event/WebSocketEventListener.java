package com.shan.chat.adapter.in.websocket.member.event;

import com.shan.chat.application.member.port.in.ConnectMemberUseCase;
import com.shan.chat.application.member.port.in.DisconnectMemberUseCase;
import com.shan.chat.application.member.port.in.SyncPresenceUseCase;
import com.shan.chat.application.room.port.in.SyncRoomPresenceUseCase;
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
    private final SyncRoomPresenceUseCase syncRoomPresenceUseCase;

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
     * 클라이언트가 특정 토픽을 구독하는 순간 현재 상태를 즉시 브로드캐스트한다.
     *
     * - /topic/presence        → 현재 접속자 목록 전송
     * - /topic/rooms           → 현재 방 목록 전송
     * - /topic/room/{id}/participants → 해당 방 참여자 목록 전송
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) return;

        log.debug("[WS subscribe] destination={}", destination);

        if ("/topic/presence".equals(destination)) {
            syncPresenceUseCase.syncPresence();
            return;
        }

        if ("/topic/rooms".equals(destination)) {
            syncRoomPresenceUseCase.syncRoomList();
            return;
        }

        // /topic/room/{roomId}/participants
        if (destination.startsWith("/topic/room/") && destination.endsWith("/participants")) {
            String[] parts = destination.split("/");
            // parts: ["", "topic", "room", "{roomId}", "participants"]
            if (parts.length == 5) {
                String roomId = parts[3];
                try {
                    syncRoomPresenceUseCase.syncParticipants(roomId);
                } catch (Exception e) {
                    log.warn("[WS subscribe] 방 참여자 동기화 실패: roomId={}, error={}", roomId, e.getMessage());
                }
            }
        }
    }

    /**
     * STOMP 연결 해제 처리.
     * 방 퇴장은 STOMP 연결 해제 시 처리하지 않는다.
     * 방 퇴장은 클라이언트에서 명시적으로 /api/rooms/{roomId}/leave 를 호출하거나
     * beforeunload 시 sendBeacon 으로 처리한다.
     */
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
