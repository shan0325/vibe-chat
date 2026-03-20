package com.shan.chat.domain.member;

import com.shan.chat.common.exception.ChatException;
import lombok.Getter;

/**
 * 멤버 도메인 모델
 * 프레임워크에 의존하지 않는 순수 도메인 객체
 */
@Getter
public class MemberProfile {

    private final String memberId;
    private String nickname;
    private boolean randomNickname;

    private MemberProfile(String memberId, String nickname, boolean randomNickname) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.randomNickname = randomNickname;
    }

    /** 신규 멤버 생성 (랜덤 닉네임) */
    public static MemberProfile create(String memberId, String randomNickname) {
        return new MemberProfile(memberId, randomNickname, true);
    }

    /** 영속 계층에서 복원 */
    public static MemberProfile restore(String memberId, String nickname, boolean randomNickname) {
        return new MemberProfile(memberId, nickname, randomNickname);
    }

    /**
     * 닉네임 변경
     * 변경 후 randomNickname 플래그를 false 로 전환한다.
     */
    public void changeNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new ChatException("닉네임은 빈 값일 수 없습니다.");
        }
        if (newNickname.length() > 20) {
            throw new ChatException("닉네임은 20자 이하여야 합니다.");
        }
        this.nickname = newNickname;
        this.randomNickname = false;
    }
}

