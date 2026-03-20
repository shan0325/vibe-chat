package com.shan.chat.application.lobby.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.in.SendLobbyMessageUseCase;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.common.exception.ChatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LobbyService implements SendLobbyMessageUseCase {

    private final ManageOnlineSessionPort manageOnlineSessionPort;
    private final BroadcastLobbyPort broadcastLobbyPort;

    @Override
    public void send(String sessionId, String content) {
        var sender = manageOnlineSessionPort.getMemberBySessionId(sessionId)
                .orElseThrow(() -> new ChatException("세션을 찾을 수 없습니다: " + sessionId));

        broadcastLobbyPort.broadcastMessage(
                LobbyMessageDto.builder()
                        .senderId(sender.getMemberId())
                        .senderNickname(sender.getNickname())
                        .content(content)
                        .sentAt(nowHHmm())
                        .type(LobbyMessageDto.MessageType.TEXT)
                        .build()
        );
    }

    private String nowHHmm() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }
}

