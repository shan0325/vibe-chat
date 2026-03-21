package com.shan.chat.adapter.out.persistence.lobby.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
import com.shan.chat.adapter.out.persistence.lobby.entity.QLobbyMessageJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Querydsl 기반 로비 메시지 조회 레포지토리.
 */
@Repository
@RequiredArgsConstructor
public class LobbyMessageQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QLobbyMessageJpaEntity lobbyMessage =
            QLobbyMessageJpaEntity.lobbyMessageJpaEntity;

    /**
     * 최근 로비 메시지를 limit 개수만큼 내림차순으로 조회한다.
     * Spring Data Pageable 대신 Querydsl limit()을 사용해 타입 안정성을 확보한다.
     */
    public List<LobbyMessageJpaEntity> findRecent(int limit) {
        return queryFactory
                .selectFrom(lobbyMessage)
                .orderBy(lobbyMessage.sentAt.desc())
                .limit(limit)
                .fetch();
    }
}

