package com.shan.chat.adapter.out.persistence.mapper;

import com.shan.chat.adapter.out.persistence.entity.MemberJpaEntity;
import com.shan.chat.domain.member.MemberProfile;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    /** 도메인 → 신규 JPA 엔티티 (createdAt 은 @CreationTimestamp 가 자동 설정) */
    public MemberJpaEntity toNewEntity(MemberProfile domain) {
        return MemberJpaEntity.builder()
                .memberId(domain.getMemberId())
                .nickname(domain.getNickname())
                .randomNickname(domain.isRandomNickname())
                .build();
    }

    /** JPA 엔티티 → 도메인 복원 */
    public MemberProfile toDomain(MemberJpaEntity entity) {
        return MemberProfile.restore(
                entity.getMemberId(),
                entity.getNickname(),
                entity.isRandomNickname()
        );
    }
}

