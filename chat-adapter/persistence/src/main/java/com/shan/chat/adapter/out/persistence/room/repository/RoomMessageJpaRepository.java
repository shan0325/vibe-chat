package com.shan.chat.adapter.out.persistence.room.repository;

import com.shan.chat.adapter.out.persistence.room.entity.RoomMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMessageJpaRepository extends JpaRepository<RoomMessageJpaEntity, Long> {
}
