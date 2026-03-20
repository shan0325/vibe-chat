package com.shan.chat.application.lobby.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.in.GetLobbyHistoryUseCase;
import com.shan.chat.application.lobby.port.in.SendLobbyMessageUseCase;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.lobby.port.out.LoadLobbyHistoryPort;
import com.shan.chat.application.lobby.port.out.SaveLobbyMessagePort;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.domain.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LobbyService implements SendLobbyMessageUseCase, GetLobbyHistoryUseCase {

    private final ManageOnlineSessionPort manageOnlineSessionPort;
    private final BroadcastLobbyPort broadcastLobbyPort;
    private final SaveLobbyMessagePort saveLobbyMessagePort;
    private final LoadLobbyHistoryPort loadLobbyHistoryPort;

    @Override
    @Transactional
    public void send(String sessionId, String content) {
        var sender = manageOnlineSessionPort.getMemberBySessionId(sessionId)
                .orElseThrow(() -> new ChatException("세션을 찾을 수 없습니다: " + sessionId));

        LocalDateTime now = LocalDateTime.now();

        saveLobbyMessagePort.save(
                ChatMessage.builder()
                        .senderId(sender.getMemberId())
                        .senderNickname(sender.getNickname())
                        .content(content)
                        .type(ChatMessage.MessageType.TEXT)
                        .sentAt(now)
                        .build()
        );

        broadcastLobbyPort.broadcastMessage(
                LobbyMessageDto.builder()
                        .senderId(sender.getMemberId())
                        .senderNickname(sender.getNickname())
                        .content(content)
                        .sentAt(formatHHmm(now))
                        .type(LobbyMessageDto.MessageType.TEXT)
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LobbyMessageDto> getRecentHistory(int limit) {
        return loadLobbyHistoryPort.loadRecent(limit);
    }

    private String formatHHmm(LocalDateTime dateTime) {
        return String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }
}

