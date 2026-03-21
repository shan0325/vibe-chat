package com.shan.chat.adapter.out.persistence.room.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.QChatRoomJpaEntity;
import com.shan.chat.adapter.out.persistence.room.entity.QRoomParticipantJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Querydsl 기반 채팅방 조회 레포지토리.
 * Dynamic Predicate 및 JOIN 기반 조회를 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class RoomQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QChatRoomJpaEntity room = QChatRoomJpaEntity.chatRoomJpaEntity;
    private static final QRoomParticipantJpaEntity participant = QRoomParticipantJpaEntity.roomParticipantJpaEntity;

    /**
     * 활성 GROUP 방을 키워드로 검색한다.
     * keyword가 null 또는 공백이면 전체 활성 GROUP 방을 반환한다.
     */
    public List<ChatRoomJpaEntity> searchActiveGroupRooms(String keyword) {
        return queryFactory
                .selectFrom(room)
                .where(
                        room.active.isTrue(),
                        room.roomType.eq(ChatRoomJpaEntity.RoomType.GROUP),
                        containsKeyword(keyword)
                )
                .orderBy(room.createdAt.desc())
                .fetch();
    }

    /**
     * 두 멤버 사이의 DIRECT 방을 조회한다 (순서 무관).
     * 별칭 인스턴스(p1, p2)로 동일 테이블에 두 번 JOIN한다.
     */
    public Optional<ChatRoomJpaEntity> findDirectRoom(String member1Id, String member2Id) {
        QRoomParticipantJpaEntity p1 = new QRoomParticipantJpaEntity("p1");
        QRoomParticipantJpaEntity p2 = new QRoomParticipantJpaEntity("p2");

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(room)
                        .innerJoin(p1).on(p1.id.roomId.eq(room.roomId))
                        .innerJoin(p2).on(p2.id.roomId.eq(room.roomId))
                        .where(
                                room.roomType.eq(ChatRoomJpaEntity.RoomType.DIRECT),
                                p1.id.memberId.eq(member1Id),
                                p2.id.memberId.eq(member2Id)
                        )
                        .fetchOne()
        );
    }

    /**
     * 특정 멤버가 참여 중인 DIRECT 방 목록을 최신순으로 조회한다.
     */
    public List<ChatRoomJpaEntity> findDirectRoomsByMemberId(String memberId) {
        return queryFactory
                .selectFrom(room)
                .innerJoin(participant).on(participant.id.roomId.eq(room.roomId))
                .where(
                        room.roomType.eq(ChatRoomJpaEntity.RoomType.DIRECT),
                        participant.id.memberId.eq(memberId)
                )
                .orderBy(room.createdAt.desc())
                .fetch();
    }

    /**
     * 특정 방의 참여자 수를 반환한다.
     */
    public long countParticipantsByRoomId(String roomId) {
        Long count = queryFactory
                .select(participant.count())
                .from(participant)
                .where(participant.id.roomId.eq(roomId))
                .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 키워드가 없으면 null(조건 무시), 있으면 방 이름에 포함 여부 확인.
     * Querydsl Dynamic Predicate 활용.
     */
    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return room.roomName.containsIgnoreCase(keyword.trim());
    }
}
