package com.shan.chat.application.room.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.application.room.dto.RoomMessageDto;
import com.shan.chat.application.room.port.in.GetRoomHistoryUseCase;
import com.shan.chat.application.room.port.in.SendRoomMessageUseCase;
import com.shan.chat.application.room.port.out.BroadcastRoomPort;
import com.shan.chat.application.room.port.out.LoadRoomHistoryPort;
import com.shan.chat.application.room.port.out.SaveRoomMessagePort;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.domain.room.RoomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomMessageService implements SendRoomMessageUseCase, GetRoomHistoryUseCase {

    private final ManageOnlineSessionPort manageOnlineSessionPort;
    private final BroadcastRoomPort broadcastRoomPort;
    private final SaveRoomMessagePort saveRoomMessagePort;
    private final LoadRoomHistoryPort loadRoomHistoryPort;

    @Override
    @Transactional
    public void send(String sessionId, String roomId, String content) {
        MemberInfo sender = manageOnlineSessionPort.getMemberBySessionId(sessionId)
                .orElseThrow(() -> new ChatException("세션을 찾을 수 없습니다: " + sessionId));

        LocalDateTime now = LocalDateTime.now();

        saveRoomMessagePort.save(RoomMessage.builder()
                .roomId(roomId)
                .senderId(sender.getMemberId())
                .senderNickname(sender.getNickname())
                .content(content)
                .type(RoomMessage.MessageType.TEXT)
                .sentAt(now)
                .build());

        broadcastRoomPort.broadcastRoomMessage(roomId,
                RoomMessageDto.builder()
                        .roomId(roomId)
                        .senderId(sender.getMemberId())
                        .senderNickname(sender.getNickname())
                        .content(content)
                        .sentAt(String.format("%02d:%02d", now.getHour(), now.getMinute()))
                        .type(RoomMessageDto.MessageType.TEXT)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMessageDto> getRecentHistory(String roomId, int limit) {
        return loadRoomHistoryPort.loadRecent(roomId, limit);
    }
}

