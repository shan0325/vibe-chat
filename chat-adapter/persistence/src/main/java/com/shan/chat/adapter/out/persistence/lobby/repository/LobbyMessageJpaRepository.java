package com.shan.chat.adapter.out.persistence.lobby.repository;

import com.shan.chat.adapter.out.persistence.lobby.entity.LobbyMessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LobbyMessageJpaRepository extends JpaRepository<LobbyMessageJpaEntity, Long> {

    @Query("SELECT m FROM LobbyMessageJpaEntity m ORDER BY m.sentAt DESC")
    List<LobbyMessageJpaEntity> findRecent(Pageable pageable);
}

