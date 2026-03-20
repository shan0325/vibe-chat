package com.shan.chat.adapter.out.persistence.lobby;

import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
import com.shan.chat.adapter.out.persistence.lobby.repository.LobbyMessageJpaRepository;
import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.LoadLobbyHistoryPort;
import com.shan.chat.application.lobby.port.out.SaveLobbyMessagePort;
import com.shan.chat.domain.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LobbyMessagePersistenceAdapter implements SaveLobbyMessagePort, LoadLobbyHistoryPort {

    private final LobbyMessageJpaRepository lobbyMessageJpaRepository;

    @Override
    public void save(ChatMessage message) {
        lobbyMessageJpaRepository.save(
                LobbyMessageJpaEntity.builder()
                        .senderId(message.getSenderId())
                        .senderNickname(message.getSenderNickname())
                        .content(message.getContent())
                        .type(LobbyMessageJpaEntity.MessageType.valueOf(message.getType().name()))
                        .sentAt(message.getSentAt())
                        .build()
        );
    }

    @Override
    public List<LobbyMessageDto> loadRecent(int limit) {
        List<LobbyMessageJpaEntity> entities =
                lobbyMessageJpaRepository.findRecent(PageRequest.of(0, limit));
        Collections.reverse(entities);
        return entities.stream()
                .map(e -> LobbyMessageDto.builder()
                        .senderId(e.getSenderId())
                        .senderNickname(e.getSenderNickname())
                        .content(e.getContent())
                        .sentAt(String.format("%02d:%02d",
                                e.getSentAt().getHour(), e.getSentAt().getMinute()))
                        .type(LobbyMessageDto.MessageType.valueOf(e.getType().name()))
                        .build())
                .collect(Collectors.toList());
    }
}

