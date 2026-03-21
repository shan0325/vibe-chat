package com.shan.chat.adapter.out.persistence.lobby;

import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
import com.shan.chat.adapter.out.persistence.lobby.query.LobbyMessageQueryRepository;
import com.shan.chat.adapter.out.persistence.lobby.repository.LobbyMessageJpaRepository;
import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.LoadLobbyHistoryPort;
import com.shan.chat.application.lobby.port.out.SaveLobbyMessagePort;
import com.shan.chat.domain.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LobbyMessagePersistenceAdapter implements SaveLobbyMessagePort, LoadLobbyHistoryPort {

    private final LobbyMessageJpaRepository lobbyMessageJpaRepository;
    private final LobbyMessageQueryRepository lobbyMessageQueryRepository;

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

    /**
     * Querydsl을 사용해 최근 로비 메시지를 조회한다.
     * Spring Data Pageable 대신 Querydsl limit()으로 타입 안정성을 확보한다.
     */
    @Override
    public List<LobbyMessageDto> loadRecent(int limit) {
        List<LobbyMessageJpaEntity> entities = lobbyMessageQueryRepository.findRecent(limit);
        Collections.reverse(entities);   // DESC 조회 후 오름차순으로 정렬
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

