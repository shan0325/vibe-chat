package com.shan.chat.adapter.out.persistence.room.repository;

import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, String> {

    List<ChatRoomJpaEntity> findByActiveTrueOrderByCreatedAtDesc();

    /** 두 멤버 사이의 DIRECT 방 조회 (순서 무관) */
    @Query("""
            SELECT r FROM ChatRoomJpaEntity r
            INNER JOIN RoomParticipantJpaEntity p1 ON p1.id.roomId = r.roomId
            INNER JOIN RoomParticipantJpaEntity p2 ON p2.id.roomId = r.roomId
            WHERE r.roomType = 'DIRECT'
              AND p1.id.memberId = :member1Id
              AND p2.id.memberId = :member2Id
            """)
    Optional<ChatRoomJpaEntity> findDirectRoom(@Param("member1Id") String member1Id,
                                               @Param("member2Id") String member2Id);

    /** 특정 멤버가 참여 중인 DIRECT 방 목록 */
    @Query("""
            SELECT r FROM ChatRoomJpaEntity r
            INNER JOIN RoomParticipantJpaEntity p ON p.id.roomId = r.roomId
            WHERE r.roomType = 'DIRECT'
              AND p.id.memberId = :memberId
            ORDER BY r.createdAt DESC
            """)
    List<ChatRoomJpaEntity> findDirectRoomsByMemberId(@Param("memberId") String memberId);
}

