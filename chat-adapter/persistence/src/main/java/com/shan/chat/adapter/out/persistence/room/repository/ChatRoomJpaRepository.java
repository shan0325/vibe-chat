package com.shan.chat.adapter.out.persistence.room.repository;

import com.shan.chat.adapter.out.persistence.room.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, String> {

    List<ChatRoomJpaEntity> findByActiveTrueOrderByCreatedAtDesc();
}
