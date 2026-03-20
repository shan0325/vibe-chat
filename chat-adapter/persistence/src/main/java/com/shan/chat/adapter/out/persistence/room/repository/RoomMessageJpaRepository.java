package com.shan.chat.adapter.out.persistence.room.repository;

import com.shan.chat.adapter.out.persistence.room.entity.RoomMessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMessageJpaRepository extends JpaRepository<RoomMessageJpaEntity, Long> {

    @Query("SELECT m FROM RoomMessageJpaEntity m WHERE m.roomId = :roomId ORDER BY m.sentAt DESC")
    List<RoomMessageJpaEntity> findRecentByRoomId(@Param("roomId") String roomId, Pageable pageable);
}

