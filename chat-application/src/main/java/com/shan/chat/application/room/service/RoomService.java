package com.shan.chat.application.room.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.dto.RoomMessageDto;
import com.shan.chat.application.room.port.in.*;
import com.shan.chat.application.room.port.out.*;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.domain.room.ChatRoom;
import com.shan.chat.domain.room.RoomMessage;
import com.shan.chat.domain.room.RoomParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService implements
        CreateRoomUseCase,
        GetRoomListUseCase,
        JoinRoomUseCase,
        LeaveRoomUseCase,
        LeaveAllRoomsUseCase,
        GetRoomParticipantsUseCase,
        SyncRoomPresenceUseCase {

    private final LoadRoomPort loadRoomPort;
    private final SaveRoomPort saveRoomPort;
    private final LoadRoomParticipantPort loadRoomParticipantPort;
    private final SaveRoomParticipantPort saveRoomParticipantPort;
    private final BroadcastRoomPort broadcastRoomPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveRoomMessagePort saveRoomMessagePort;

    @Override
    @Transactional
    public RoomDto create(String memberId, String roomName) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom room = ChatRoom.create(roomId, roomName, memberId);
        saveRoomPort.save(room);

        // 방 생성자 자동 입장
        saveRoomParticipantPort.save(RoomParticipant.join(roomId, memberId));

        // 시스템 메시지 저장
        String nickname = getNickname(memberId);
        saveRoomMessagePort.save(systemRoomMsg(roomId, nickname + "님이 방을 생성했습니다."));

        int count = loadRoomParticipantPort.countByRoomId(roomId);
        RoomDto dto = toRoomDto(room, count);

        // 방 목록 브로드캐스트
        broadcastRoomList();

        log.info("[방 생성] roomId={}, roomName={}, creatorId={}", roomId, roomName, memberId);
        return dto;
    }

    @Override
    @Transactional
    public void join(String memberId, String roomId) {
        ChatRoom room = loadRoomPort.loadById(roomId)
                .orElseThrow(() -> new ChatException("존재하지 않는 방입니다: " + roomId));

        if (!room.isActive()) throw new ChatException("비활성화된 방입니다: " + roomId);

        boolean isNew = !loadRoomParticipantPort.existsByRoomIdAndMemberId(roomId, memberId);

        if (isNew) {
            saveRoomParticipantPort.save(RoomParticipant.join(roomId, memberId));
            String nickname = getNickname(memberId);
            saveRoomMessagePort.save(systemRoomMsg(roomId, nickname + "님이 입장했습니다."));
            broadcastRoomPort.broadcastRoomMessage(roomId, systemRoomMessageDto(roomId, nickname + "님이 입장했습니다."));
            broadcastRoomList();
        }

        // 이미 참여 중이어도 항상 참여자 목록을 브로드캐스트한다.
        // (페이지 새로고침, 재접속 등 다양한 케이스 대응)
        List<MemberInfo> participants = loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
        broadcastRoomPort.broadcastRoomParticipants(roomId, participants);

        log.info("[방 입장] roomId={}, memberId={}, isNew={}", roomId, memberId, isNew);
    }

    @Override
    @Transactional
    public void leave(String memberId, String roomId) {
        if (!loadRoomParticipantPort.existsByRoomIdAndMemberId(roomId, memberId)) return;

        saveRoomParticipantPort.deleteByRoomIdAndMemberId(roomId, memberId);

        String nickname = getNickname(memberId);
        saveRoomMessagePort.save(systemRoomMsg(roomId, nickname + "님이 퇴장했습니다."));

        List<MemberInfo> participants = loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
        RoomMessageDto sysMsg = systemRoomMessageDto(roomId, nickname + "님이 퇴장했습니다.");

        broadcastRoomPort.broadcastRoomMessage(roomId, sysMsg);
        broadcastRoomPort.broadcastRoomParticipants(roomId, participants);
        broadcastRoomList();

        log.info("[방 퇴장] roomId={}, memberId={}", roomId, memberId);
    }

    @Override
    @Transactional
    public void leaveAllRooms(String memberId) {
        List<String> roomIds = loadRoomParticipantPort.loadRoomIdsByMemberId(memberId);
        roomIds.forEach(roomId -> leave(memberId, roomId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomList() {
        return loadRoomPort.loadAllActive().stream()
                .map(room -> toRoomDto(room, loadRoomParticipantPort.countByRoomId(room.getRoomId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomDto> getRoomById(String roomId) {
        return loadRoomPort.loadById(roomId)
                .map(room -> toRoomDto(room, loadRoomParticipantPort.countByRoomId(roomId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberInfo> getParticipants(String roomId) {
        return loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public void syncParticipants(String roomId) {
        List<MemberInfo> participants = loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
        broadcastRoomPort.broadcastRoomParticipants(roomId, participants);
    }

    @Override
    @Transactional(readOnly = true)
    public void syncRoomList() {
        List<RoomDto> rooms = loadRoomPort.loadAllActive().stream()
                .map(room -> toRoomDto(room, loadRoomParticipantPort.countByRoomId(room.getRoomId())))
                .collect(Collectors.toList());
        broadcastRoomPort.broadcastRoomList(rooms);
    }

    // ──────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────

    private void broadcastRoomList() {
        List<RoomDto> rooms = getRoomList();
        broadcastRoomPort.broadcastRoomList(rooms);
    }

    private String getNickname(String memberId) {
        return loadMemberPort.loadByMemberId(memberId)
                .map(m -> m.getNickname())
                .orElse("알 수 없음");
    }

    private RoomDto toRoomDto(ChatRoom room, int count) {
        return RoomDto.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .creatorId(room.getCreatorId())
                .participantCount(count)
                .createdAt(room.getCreatedAt())
                .build();
    }

    private RoomMessage systemRoomMsg(String roomId, String content) {
        return RoomMessage.builder()
                .roomId(roomId)
                .content(content)
                .type(RoomMessage.MessageType.SYSTEM)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private RoomMessageDto systemRoomMessageDto(String roomId, String content) {
        LocalDateTime now = LocalDateTime.now();
        return RoomMessageDto.builder()
                .roomId(roomId)
                .content(content)
                .sentAt(String.format("%02d:%02d", now.getHour(), now.getMinute()))
                .type(RoomMessageDto.MessageType.SYSTEM)
                .build();
    }
}

