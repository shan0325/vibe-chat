package com.shan.chat.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberJpaEntity {

    @Id
    @Column(name = "member_id", length = 36)
    private String memberId;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "random_nickname", nullable = false)
    private boolean randomNickname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 닉네임 업데이트 — JPA 더티 체킹으로 트랜잭션 종료 시 자동 반영 */
    public void updateNickname(String nickname, boolean randomNickname) {
        this.nickname = nickname;
        this.randomNickname = randomNickname;
    }
}

