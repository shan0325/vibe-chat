package com.shan.chat.adapter.out.persistence.repository;

import com.shan.chat.adapter.out.persistence.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, String> {
}

