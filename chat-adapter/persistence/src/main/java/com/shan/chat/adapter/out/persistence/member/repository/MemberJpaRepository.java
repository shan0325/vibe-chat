package com.shan.chat.adapter.out.persistence.member.repository;

import com.shan.chat.adapter.out.persistence.member.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, String> {
}

