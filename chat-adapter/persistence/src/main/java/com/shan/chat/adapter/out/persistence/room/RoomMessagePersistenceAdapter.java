package com.shan.chat.adapter.out.persistence.room;

import com.shan.chat.adapter.out.persistence.room.entity.RoomMessageJpaEntity;
import com.shan.chat.adapter.out.persistence.room.repository.RoomMessageJpaRepository;
import com.shan.chat.application.room.dto.RoomMessageDto;
import com.shan.chat.application.room.port.out.LoadRoomHistoryPort;
import com.shan.chat.application.room.port.out.SaveRoomMessagePort;
import com.shan.chat.domain.room.RoomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomMessagePersistenceAdapter implements SaveRoomMessagePort, LoadRoomHistoryPort {

    private final RoomMessageJpaRepository roomMessageJpaRepository;

    @Override
    public void save(RoomMessage message) {
        roomMessageJpaRepository.save(RoomMessageJpaEntity.builder()
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderNickname(message.getSenderNickname())
                .content(message.getContent())
                .type(RoomMessageJpaEntity.MessageType.valueOf(message.getType().name()))
                .sentAt(message.getSentAt())
                .build());
    }

    @Override
    public List<RoomMessageDto> loadRecent(String roomId, int limit) {
        List<RoomMessageJpaEntity> entities =
                roomMessageJpaRepository.findRecentByRoomId(roomId, PageRequest.of(0, limit));
        Collections.reverse(entities);
        return entities.stream()
                .map(e -> RoomMessageDto.builder()
                        .roomId(e.getRoomId())
                        .senderId(e.getSenderId())
                        .senderNickname(e.getSenderNickname())
                        .content(e.getContent())
                        .sentAt(String.format("%02d:%02d",
                                e.getSentAt().getHour(), e.getSentAt().getMinute()))
                        .type(RoomMessageDto.MessageType.valueOf(e.getType().name()))
                        .build())
                .collect(Collectors.toList());
    }
}

