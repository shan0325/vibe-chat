package com.shan.chat.adapter.out.persistence.room.query;

import com.shan.chat.adapter.out.persistence.PersistenceTestApplication;
import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.RoomMessageJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PersistenceTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RoomMessageQueryRepositoryTest {

    @Autowired
    private RoomMessageQueryRepository roomMessageQueryRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("특정 방의 최근 메시지를 limit 개수만큼 내림차순으로 조회한다")
    void findRecentByRoomId_returnsLimitedDescResults() {
        persistRoom("r1");
        LocalDateTime base = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            em.persist(roomMsg("r1", "msg-" + i, base.plusSeconds(i)));
        }
        em.flush(); em.clear();

        List<RoomMessageJpaEntity> result = roomMessageQueryRepository.findRecentByRoomId("r1", 3);

        assertThat(result).hasSize(3);
        // DESC 조회이므로 가장 최신(msg-5)이 먼저
        assertThat(result.get(0).getContent()).isEqualTo("msg-5");
        assertThat(result.get(1).getContent()).isEqualTo("msg-4");
        assertThat(result.get(2).getContent()).isEqualTo("msg-3");
    }

    @Test
    @DisplayName("다른 방의 메시지는 포함되지 않는다")
    void findRecentByRoomId_excludesOtherRooms() {
        persistRoom("r1");
        persistRoom("r2");
        em.persist(roomMsg("r1", "r1-msg", LocalDateTime.now()));
        em.persist(roomMsg("r2", "r2-msg", LocalDateTime.now()));
        em.flush(); em.clear();

        List<RoomMessageJpaEntity> result = roomMessageQueryRepository.findRecentByRoomId("r1", 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("r1-msg");
    }

    @Test
    @DisplayName("메시지가 없으면 빈 목록을 반환한다")
    void findRecentByRoomId_noMessages_returnsEmptyList() {
        persistRoom("r1");
        em.flush(); em.clear();

        List<RoomMessageJpaEntity> result = roomMessageQueryRepository.findRecentByRoomId("r1", 50);

        assertThat(result).isEmpty();
    }

    // ── Private helpers ───────────────────────────────────────

    private void persistRoom(String roomId) {
        em.persist(ChatRoomJpaEntity.builder()
                .roomId(roomId)
                .roomName("방-" + roomId)
                .creatorId("creator")
                .createdAt(LocalDateTime.now())
                .active(true)
                .roomType(ChatRoomJpaEntity.RoomType.GROUP)
                .build());
    }

    private RoomMessageJpaEntity roomMsg(String roomId, String content, LocalDateTime sentAt) {
        return RoomMessageJpaEntity.builder()
                .roomId(roomId)
                .senderId("sender-1")
                .senderNickname("철수")
                .content(content)
                .type(RoomMessageJpaEntity.MessageType.TEXT)
                .sentAt(sentAt)
                .build();
    }
}
