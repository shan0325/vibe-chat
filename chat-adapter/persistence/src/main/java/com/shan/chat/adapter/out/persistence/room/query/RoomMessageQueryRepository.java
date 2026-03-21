package com.shan.chat.adapter.out.persistence.room.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shan.chat.adapter.out.persistence.room.entity.QRoomMessageJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.RoomMessageJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Querydsl 기반 방 메시지 조회 레포지토리.
 */
@Repository
@RequiredArgsConstructor
public class RoomMessageQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QRoomMessageJpaEntity roomMessage =
            QRoomMessageJpaEntity.roomMessageJpaEntity;

    /**
     * 특정 방의 최근 메시지를 limit 개수만큼 내림차순으로 조회한다.
     */
    public List<RoomMessageJpaEntity> findRecentByRoomId(String roomId, int limit) {
        return queryFactory
                .selectFrom(roomMessage)
                .where(roomMessage.roomId.eq(roomId))
                .orderBy(roomMessage.sentAt.desc())
                .limit(limit)
                .fetch();
    }
}

