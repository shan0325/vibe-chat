package com.shan.chat.adapter.out.persistence.room.query;

import com.shan.chat.adapter.out.persistence.PersistenceTestApplication;
import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantId;
import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PersistenceTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RoomQueryRepositoryTest {

    @Autowired
    private RoomQueryRepository roomQueryRepository;

    @Autowired
    private EntityManager em;

    // ── searchActiveGroupRooms ────────────────────────────────

    @Test
    @DisplayName("키워드 없이 조회 시 전체 활성 GROUP 방을 반환한다")
    void searchActiveGroupRooms_noKeyword_returnsAll() {
        persistRoom("r1", "자바 스터디", "c1", true,  ChatRoomJpaEntity.RoomType.GROUP);
        persistRoom("r2", "파이썬 모임", "c1", true,  ChatRoomJpaEntity.RoomType.GROUP);
        persistRoom("r3", "1:1방",       "c1", true,  ChatRoomJpaEntity.RoomType.DIRECT);
        persistRoom("r4", "비활성방",     "c1", false, ChatRoomJpaEntity.RoomType.GROUP);
        em.flush(); em.clear();

        List<ChatRoomJpaEntity> result = roomQueryRepository.searchActiveGroupRooms(null);

        assertThat(result).hasSize(2)
                .extracting(ChatRoomJpaEntity::getRoomName)
                .containsExactlyInAnyOrder("자바 스터디", "파이썬 모임");
    }

    @Test
    @DisplayName("키워드로 조회 시 방 이름에 키워드를 포함한 GROUP 방만 반환한다")
    void searchActiveGroupRooms_withKeyword_filtersResults() {
        persistRoom("r1", "자바 스터디", "c1", true, ChatRoomJpaEntity.RoomType.GROUP);
        persistRoom("r2", "파이썬 모임", "c1", true, ChatRoomJpaEntity.RoomType.GROUP);
        em.flush(); em.clear();

        List<ChatRoomJpaEntity> result = roomQueryRepository.searchActiveGroupRooms("자바");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomName()).isEqualTo("자바 스터디");
    }

    @Test
    @DisplayName("검색 결과는 최신 생성 순으로 정렬된다")
    void searchActiveGroupRooms_orderedByCreatedAtDesc() {
        persistRoom("r1", "오래된방", "c1", true, ChatRoomJpaEntity.RoomType.GROUP);
        persistRoom("r2", "새로운방", "c1", true, ChatRoomJpaEntity.RoomType.GROUP);
        em.flush(); em.clear();

        List<ChatRoomJpaEntity> result = roomQueryRepository.searchActiveGroupRooms(null);

        // 최신(r2)이 먼저 나와야 한다
        assertThat(result.get(0).getRoomId()).isEqualTo("r2");
    }

    // ── findDirectRoom ────────────────────────────────────────

    @Test
    @DisplayName("두 멤버 사이의 DIRECT 방을 조회한다")
    void findDirectRoom_returnsDirectRoom() {
        persistRoom("r1", "direct", "m1", true, ChatRoomJpaEntity.RoomType.DIRECT);
        persistParticipant("r1", "m1");
        persistParticipant("r1", "m2");
        em.flush(); em.clear();

        Optional<ChatRoomJpaEntity> result = roomQueryRepository.findDirectRoom("m1", "m2");

        assertThat(result).isPresent();
        assertThat(result.get().getRoomId()).isEqualTo("r1");
    }

    @Test
    @DisplayName("DIRECT 방이 없으면 Optional.empty()를 반환한다")
    void findDirectRoom_notExists_returnsEmpty() {
        Optional<ChatRoomJpaEntity> result = roomQueryRepository.findDirectRoom("m1", "m2");

        assertThat(result).isEmpty();
    }

    // ── findDirectRoomsByMemberId ─────────────────────────────

    @Test
    @DisplayName("특정 멤버가 참여 중인 DIRECT 방 목록을 조회한다")
    void findDirectRoomsByMemberId_returnsList() {
        persistRoom("r1", "direct-1", "m1", true, ChatRoomJpaEntity.RoomType.DIRECT);
        persistRoom("r2", "direct-2", "m1", true, ChatRoomJpaEntity.RoomType.DIRECT);
        persistRoom("r3", "group",    "m1", true, ChatRoomJpaEntity.RoomType.GROUP);
        persistParticipant("r1", "m1");
        persistParticipant("r2", "m1");
        persistParticipant("r3", "m1");
        em.flush(); em.clear();

        List<ChatRoomJpaEntity> result = roomQueryRepository.findDirectRoomsByMemberId("m1");

        assertThat(result).hasSize(2)
                .allMatch(r -> r.getRoomType() == ChatRoomJpaEntity.RoomType.DIRECT);
    }

    // ── countParticipantsByRoomId ─────────────────────────────

    @Test
    @DisplayName("방 참여자 수를 반환한다")
    void countParticipantsByRoomId_returnsCount() {
        persistRoom("r1", "스터디방", "m1", true, ChatRoomJpaEntity.RoomType.GROUP);
        persistParticipant("r1", "m1");
        persistParticipant("r1", "m2");
        persistParticipant("r1", "m3");
        em.flush(); em.clear();

        long count = roomQueryRepository.countParticipantsByRoomId("r1");

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("참여자가 없는 방의 참여자 수는 0이다")
    void countParticipantsByRoomId_empty_returnsZero() {
        persistRoom("r1", "스터디방", "m1", true, ChatRoomJpaEntity.RoomType.GROUP);
        em.flush(); em.clear();

        long count = roomQueryRepository.countParticipantsByRoomId("r1");

        assertThat(count).isZero();
    }

    // ── Private helpers ───────────────────────────────────────

    private void persistRoom(String roomId, String roomName, String creatorId,
                             boolean active, ChatRoomJpaEntity.RoomType type) {
        em.persist(ChatRoomJpaEntity.builder()
                .roomId(roomId)
                .roomName(roomName)
                .creatorId(creatorId)
                .createdAt(LocalDateTime.now())
                .active(active)
                .roomType(type)
                .build());
    }

    private void persistParticipant(String roomId, String memberId) {
        em.persist(RoomParticipantJpaEntity.builder()
                .id(new RoomParticipantId(roomId, memberId))
                .joinedAt(LocalDateTime.now())
                .build());
    }
}
