package com.shan.chat.application.room.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService implements
        CreateRoomUseCase,
        GetRoomListUseCase,
        JoinRoomUseCase,
        LeaveRoomUseCase,
        LeaveAllRoomsUseCase,
        GetRoomParticipantsUseCase,
        SyncRoomPresenceUseCase,
        SyncDirectPresenceUseCase,
        StartDirectChatUseCase {

    private final LoadRoomPort loadRoomPort;
    private final SaveRoomPort saveRoomPort;
    private final LoadRoomParticipantPort loadRoomParticipantPort;
    private final SaveRoomParticipantPort saveRoomParticipantPort;
    private final BroadcastRoomPort broadcastRoomPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveRoomMessagePort saveRoomMessagePort;
    private final FindDirectRoomPort findDirectRoomPort;
    private final ManageOnlineSessionPort manageOnlineSessionPort;

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

        // DIRECT 방은 startDirectChat() 으로 초대된 참여자만 접근 가능
        if (room.getRoomType() == ChatRoom.RoomType.DIRECT && isNew) {
            throw new ChatException("1:1 채팅방에 참여할 권한이 없습니다.");
        }

        if (isNew) {
            saveRoomParticipantPort.save(RoomParticipant.join(roomId, memberId));
            String nickname = getNickname(memberId);
            saveRoomMessagePort.save(systemRoomMsg(roomId, nickname + "님이 입장했습니다."));
            broadcastRoomPort.broadcastRoomMessage(roomId, systemRoomMessageDto(roomId, nickname + "님이 입장했습니다."));
            if (room.getRoomType() == ChatRoom.RoomType.GROUP) {
                broadcastRoomList();
            }
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

        ChatRoom room = loadRoomPort.loadById(roomId).orElse(null);

        saveRoomParticipantPort.deleteByRoomIdAndMemberId(roomId, memberId);

        String nickname = getNickname(memberId);
        saveRoomMessagePort.save(systemRoomMsg(roomId, nickname + "님이 퇴장했습니다."));

        List<MemberInfo> participants = loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
        RoomMessageDto sysMsg = systemRoomMessageDto(roomId, nickname + "님이 퇴장했습니다.");

        broadcastRoomPort.broadcastRoomMessage(roomId, sysMsg);
        broadcastRoomPort.broadcastRoomParticipants(roomId, participants);

        // DIRECT 방 퇴장은 GROUP 방 목록과 무관하므로 브로드캐스트 생략
        if (room == null || room.getRoomType() == ChatRoom.RoomType.GROUP) {
            broadcastRoomList();
        }

        log.info("[방 퇴장] roomId={}, memberId={}", roomId, memberId);
    }

    @Override
    @Transactional
    public void leaveAllRooms(String memberId) {
        List<String> roomIds = loadRoomParticipantPort.loadRoomIdsByMemberId(memberId);
        roomIds.forEach(roomId -> leave(memberId, roomId));
    }

    /** GROUP 방 목록만 반환 */
    @Override
    public List<RoomDto> getRoomList() {
        return loadRoomPort.loadAllActive().stream()
                .filter(room -> room.getRoomType() == com.shan.chat.domain.room.ChatRoom.RoomType.GROUP)
                .map(room -> toRoomDto(room, loadRoomParticipantPort.countByRoomId(room.getRoomId())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoomDto> getRoomById(String roomId) {
        return loadRoomPort.loadById(roomId)
                .map(room -> toRoomDto(room, loadRoomParticipantPort.countByRoomId(roomId)));
    }

    @Override
    public List<MemberInfo> getParticipants(String roomId) {
        return loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId);
    }

    @Override
    public void syncParticipants(String roomId) {
        // STOMP 구독 기반 live 목록을 참여자 목록으로 사용
        List<MemberInfo> participants = manageOnlineSessionPort.getMembersInRoom(roomId);
        broadcastRoomPort.broadcastRoomParticipants(roomId, participants);
    }

    @Override
    public void syncRoomList() {
        // GROUP 방 목록만 브로드캐스트 — DIRECT 방은 제3자에게 노출하지 않는다
        broadcastRoomPort.broadcastRoomList(getRoomList());
    }

    @Override
    @Transactional
    public RoomDto startDirectChat(String requesterId, String targetMemberId) {
        // 기존 1:1 방 조회
        Optional<ChatRoom> existing = findDirectRoomPort.findDirectRoom(requesterId, targetMemberId);
        if (existing.isPresent()) {
            ChatRoom room = existing.get();
            // 혹시 퇴장 상태면 재입장
            if (!loadRoomParticipantPort.existsByRoomIdAndMemberId(room.getRoomId(), requesterId)) {
                saveRoomParticipantPort.save(RoomParticipant.join(room.getRoomId(), requesterId));
            }
            List<MemberInfo> participants = loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(room.getRoomId());
            broadcastRoomPort.broadcastRoomParticipants(room.getRoomId(), participants);

            notifyDirectChatToMembers(requesterId, targetMemberId);
            return toRoomDto(room, countOnlineParticipants(room.getRoomId()));
        }

        // 새 1:1 방 생성
        String roomId = UUID.randomUUID().toString();
        String requesterNick = getNickname(requesterId);
        String targetNick    = getNickname(targetMemberId);
        String roomName      = requesterNick + " ↔ " + targetNick;

        ChatRoom room = ChatRoom.createDirect(roomId, roomName, requesterId);
        saveRoomPort.save(room);

        saveRoomParticipantPort.save(RoomParticipant.join(roomId, requesterId));
        saveRoomParticipantPort.save(RoomParticipant.join(roomId, targetMemberId));

        saveRoomMessagePort.save(systemRoomMsg(roomId, "1:1 채팅이 시작되었습니다."));

        notifyDirectChatToMembers(requesterId, targetMemberId);

        log.info("[1:1 방 생성] roomId={}, requester={}, target={}", roomId, requesterId, targetMemberId);
        return toRoomDto(room, countOnlineParticipants(roomId));
    }

    /** 두 멤버 모두에게 1:1 대화 목록을 실시간으로 전송한다. */
    private void notifyDirectChatToMembers(String requesterId, String targetMemberId) {
        broadcastRoomPort.notifyDirectChatUpdate(requesterId, getMyDirectRooms(requesterId));
        broadcastRoomPort.notifyDirectChatUpdate(targetMemberId, getMyDirectRooms(targetMemberId));
    }

    /**
     * 멤버 접속/퇴장 시 해당 멤버 및 1:1 대화 상대방들의 온라인 수를 실시간 갱신한다.
     */
    @Override
    public void syncDirectPresence(String memberId) {
        List<ChatRoom> myDirectRooms = findDirectRoomPort.findDirectRoomsByMemberId(memberId);
        if (myDirectRooms.isEmpty()) return;

        // 본인 사이드바 갱신
        broadcastRoomPort.notifyDirectChatUpdate(memberId, getMyDirectRooms(memberId));

        // 각 1:1 대화 상대방 사이드바 갱신
        myDirectRooms.forEach(room ->
                loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(room.getRoomId()).stream()
                        .filter(p -> !p.getMemberId().equals(memberId))
                        .forEach(partner -> broadcastRoomPort.notifyDirectChatUpdate(
                                partner.getMemberId(), getMyDirectRooms(partner.getMemberId())))
        );
    }

    @Override
    public void syncDirectPresenceForRoom(String roomId) {
        ChatRoom room = loadRoomPort.loadById(roomId).orElse(null);
        if (room == null || room.getRoomType() != ChatRoom.RoomType.DIRECT) return;

        // 해당 방 참여자 전원의 사이드바를 새 입장 수로 갱신
        loadRoomParticipantPort.loadParticipantsWithInfoByRoomId(roomId).forEach(p ->
                broadcastRoomPort.notifyDirectChatUpdate(p.getMemberId(), getMyDirectRooms(p.getMemberId()))
        );
    }

    @Override
    public List<RoomDto> getMyDirectRooms(String memberId) {
        return findDirectRoomPort.findDirectRoomsByMemberId(memberId).stream()
                .map(room -> toRoomDto(room, countOnlineParticipants(room.getRoomId())))
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────

    /**
     * 현재 해당 방 페이지를 열고 있는 멤버 수를 반환한다.
     * STOMP /topic/room/{roomId} 구독 기준으로 추적된다.
     */
    private int countOnlineParticipants(String roomId) {
        return manageOnlineSessionPort.countMembersInRoom(roomId);
    }

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
                .roomType(room.getRoomType().name())
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
