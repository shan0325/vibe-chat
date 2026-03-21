package com.shan.chat.adapter.out.persistence.lobby.repository;

import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LobbyMessageJpaRepository extends JpaRepository<LobbyMessageJpaEntity, Long> {
}
