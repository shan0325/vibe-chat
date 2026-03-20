package com.shan.chat.adapter.in.websocket.broadcast;

import com.shan.chat.application.dto.LobbyMessageDto;
import com.shan.chat.application.dto.MemberInfo;
import com.shan.chat.application.port.out.BroadcastLobbyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** SimpMessagingTemplate 을 이용한 로비 브로드캐스트 어댑터 */
@Component
@RequiredArgsConstructor
public class LobbyBroadcastAdapter implements BroadcastLobbyPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastMessage(LobbyMessageDto message) {
        messagingTemplate.convertAndSend("/topic/lobby", message);
    }

    @Override
    public void broadcastPresence(List<MemberInfo> onlineMembers) {
        messagingTemplate.convertAndSend("/topic/presence", onlineMembers);
    }
}

