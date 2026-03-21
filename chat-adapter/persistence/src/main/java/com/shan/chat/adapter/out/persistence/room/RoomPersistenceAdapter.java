package com.shan.chat.adapter.out.persistence.room;

import com.shan.chat.adapter.out.persistence.member.repository.MemberJpaRepository;
import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantId;
import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantJpaEntity;
import com.shan.chat.adapter.out.persistence.room.query.RoomQueryRepository;
import com.shan.chat.adapter.out.persistence.room.repository.ChatRoomJpaRepository;
import com.shan.chat.adapter.out.persistence.room.repository.RoomParticipantJpaRepository;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.room.port.out.*;
import com.shan.chat.domain.room.ChatRoom;
import com.shan.chat.domain.room.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomPersistenceAdapter implements LoadRoomPort, SaveRoomPort,
        LoadRoomParticipantPort, SaveRoomParticipantPort, FindDirectRoomPort, SearchRoomPort {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final RoomParticipantJpaRepository roomParticipantJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final RoomQueryRepository roomQueryRepository;

    // ── LoadRoomPort ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatRoom> loadById(String roomId) {
        return chatRoomJpaRepository.findById(roomId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> loadAllActive() {
        return chatRoomJpaRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── SaveRoomPort ──────────────────────────────────────────

    @Override
    @Transactional
    public void save(ChatRoom room) {
        chatRoomJpaRepository.findById(room.getRoomId())
                .ifPresentOrElse(
                        entity -> { /* 현재는 불변 필드만 있으므로 별도 업데이트 없음 */ },
                        () -> chatRoomJpaRepository.save(ChatRoomJpaEntity.builder()
                                .roomId(room.getRoomId())
                                .roomName(room.getRoomName())
                                .creatorId(room.getCreatorId())
                                .createdAt(room.getCreatedAt())
                                .active(room.isActive())
                                .roomType(toEntityType(room.getRoomType()))
                                .build())
                );
    }

    // ── SearchRoomPort ────────────────────────────────────────

    /**
     * Querydsl Dynamic Predicate를 사용해 방 이름 키워드로 검색한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> searchGroupRooms(String keyword) {
        return roomQueryRepository.searchActiveGroupRooms(keyword)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── FindDirectRoomPort ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatRoom> findDirectRoom(String member1Id, String member2Id) {
        return roomQueryRepository.findDirectRoom(member1Id, member2Id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> findDirectRoomsByMemberId(String memberId) {
        return roomQueryRepository.findDirectRoomsByMemberId(memberId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── LoadRoomParticipantPort ───────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<String> loadRoomIdsByMemberId(String memberId) {
        return roomParticipantJpaRepository.findByIdMemberId(memberId)
                .stream().map(e -> e.getId().getRoomId()).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoomIdAndMemberId(String roomId, String memberId) {
        return roomParticipantJpaRepository.existsByIdRoomIdAndIdMemberId(roomId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByRoomId(String roomId) {
        return (int) roomQueryRepository.countParticipantsByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberInfo> loadParticipantsWithInfoByRoomId(String roomId) {
        return roomParticipantJpaRepository.findByIdRoomId(roomId).stream()
                .map(p -> memberJpaRepository.findById(p.getId().getMemberId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(m -> MemberInfo.builder()
                        .memberId(m.getMemberId())
                        .nickname(m.getNickname())
                        .randomNickname(m.isRandomNickname())
                        .build())
                .collect(Collectors.toList());
    }

    // ── SaveRoomParticipantPort ───────────────────────────────

    @Override
    @Transactional
    public void save(RoomParticipant participant) {
        RoomParticipantId id = new RoomParticipantId(participant.getRoomId(), participant.getMemberId());
        if (!roomParticipantJpaRepository.existsById(id)) {
            roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                    .id(id)
                    .joinedAt(participant.getJoinedAt())
                    .build());
        }
    }

    @Override
    @Transactional
    public void deleteByRoomIdAndMemberId(String roomId, String memberId) {
        roomParticipantJpaRepository.deleteByIdRoomIdAndIdMemberId(roomId, memberId);
    }

    // ── Private helpers ───────────────────────────────────────

    private ChatRoom toDomain(ChatRoomJpaEntity e) {
        ChatRoom.RoomType roomType = e.getRoomType() == ChatRoomJpaEntity.RoomType.DIRECT
                ? ChatRoom.RoomType.DIRECT
                : ChatRoom.RoomType.GROUP;
        return ChatRoom.restore(e.getRoomId(), e.getRoomName(), e.getCreatorId(),
                e.getCreatedAt(), e.isActive(), roomType);
    }

    private ChatRoomJpaEntity.RoomType toEntityType(ChatRoom.RoomType roomType) {
        return roomType == ChatRoom.RoomType.DIRECT
                ? ChatRoomJpaEntity.RoomType.DIRECT
                : ChatRoomJpaEntity.RoomType.GROUP;
    }
}

