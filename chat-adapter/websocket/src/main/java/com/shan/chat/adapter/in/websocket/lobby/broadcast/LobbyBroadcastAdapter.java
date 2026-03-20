package com.shan.chat.adapter.in.websocket.lobby.broadcast;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.dto.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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

