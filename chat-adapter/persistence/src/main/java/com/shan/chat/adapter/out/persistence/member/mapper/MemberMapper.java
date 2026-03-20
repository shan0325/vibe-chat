package com.shan.chat.adapter.out.persistence.member.mapper;

import com.shan.chat.adapter.out.persistence.member.entity.MemberJpaEntity;
import com.shan.chat.domain.member.MemberProfile;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberJpaEntity toNewEntity(MemberProfile domain) {
        return MemberJpaEntity.builder()
                .memberId(domain.getMemberId())
                .nickname(domain.getNickname())
                .randomNickname(domain.isRandomNickname())
                .build();
    }

    public MemberProfile toDomain(MemberJpaEntity entity) {
        return MemberProfile.restore(
                entity.getMemberId(),
                entity.getNickname(),
                entity.isRandomNickname()
        );
    }
}

