package com.shan.chat.adapter.out.persistence.lobby.query;

import com.shan.chat.adapter.out.persistence.PersistenceTestApplication;
import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
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
class LobbyMessageQueryRepositoryTest {

    @Autowired
    private LobbyMessageQueryRepository lobbyMessageQueryRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("최근 메시지를 limit 개수만큼 내림차순으로 조회한다")
    void findRecent_returnsLimitedDescResults() {
        LocalDateTime base = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            em.persist(lobbyMsg("sender-" + i, base.plusSeconds(i)));
        }
        em.flush(); em.clear();

        List<LobbyMessageJpaEntity> result = lobbyMessageQueryRepository.findRecent(3);

        assertThat(result).hasSize(3);
        // DESC 조회이므로 가장 최신(5번째)이 먼저 와야 한다
        assertThat(result.get(0).getSenderId()).isEqualTo("sender-5");
        assertThat(result.get(1).getSenderId()).isEqualTo("sender-4");
        assertThat(result.get(2).getSenderId()).isEqualTo("sender-3");
    }

    @Test
    @DisplayName("메시지가 limit보다 적으면 전체를 반환한다")
    void findRecent_fewerThanLimit_returnsAll() {
        em.persist(lobbyMsg("sender-1", LocalDateTime.now()));
        em.persist(lobbyMsg("sender-2", LocalDateTime.now().plusSeconds(1)));
        em.flush(); em.clear();

        List<LobbyMessageJpaEntity> result = lobbyMessageQueryRepository.findRecent(50);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("메시지가 없으면 빈 목록을 반환한다")
    void findRecent_noMessages_returnsEmptyList() {
        List<LobbyMessageJpaEntity> result = lobbyMessageQueryRepository.findRecent(50);

        assertThat(result).isEmpty();
    }

    private LobbyMessageJpaEntity lobbyMsg(String senderId, LocalDateTime sentAt) {
        return LobbyMessageJpaEntity.builder()
                .senderId(senderId)
                .senderNickname("닉네임")
                .content("메시지 내용")
                .type(LobbyMessageJpaEntity.MessageType.TEXT)
                .sentAt(sentAt)
                .build();
    }
}
