package com.shan.chat.domain.member;

import com.shan.chat.common.exception.ChatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MemberProfileTest {

    @Test
    @DisplayName("신규 멤버 생성 시 랜덤 닉네임 플래그가 true다")
    void create_setsRandomNicknameFlag() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");

        assertThat(member.getMemberId()).isEqualTo("member-1");
        assertThat(member.getNickname()).isEqualTo("귀여운고양이");
        assertThat(member.isRandomNickname()).isTrue();
    }

    @Test
    @DisplayName("닉네임 변경 시 랜덤 닉네임 플래그가 false로 전환된다")
    void changeNickname_clearsRandomFlag() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");

        member.changeNickname("철수");

        assertThat(member.getNickname()).isEqualTo("철수");
        assertThat(member.isRandomNickname()).isFalse();
    }

    @Test
    @DisplayName("닉네임이 빈 값이면 예외가 발생한다")
    void changeNickname_blank_throwsException() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");

        assertThatThrownBy(() -> member.changeNickname("  "))
                .isInstanceOf(ChatException.class)
                .hasMessageContaining("빈 값");
    }

    @Test
    @DisplayName("닉네임이 20자를 초과하면 예외가 발생한다")
    void changeNickname_tooLong_throwsException() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");
        String longNickname = "a".repeat(21);

        assertThatThrownBy(() -> member.changeNickname(longNickname))
                .isInstanceOf(ChatException.class)
                .hasMessageContaining("20자");
    }

    @Test
    @DisplayName("정확히 20자인 닉네임은 허용된다")
    void changeNickname_exactly20chars_allowed() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");
        String nickname20 = "a".repeat(20);

        assertThatCode(() -> member.changeNickname(nickname20))
                .doesNotThrowAnyException();
        assertThat(member.getNickname()).isEqualTo(nickname20);
    }

    @Test
    @DisplayName("영속 계층에서 복원한 멤버의 랜덤 닉네임 플래그가 그대로 유지된다")
    void restore_preservesFields() {
        MemberProfile member = MemberProfile.restore("member-2", "영희", false);

        assertThat(member.getMemberId()).isEqualTo("member-2");
        assertThat(member.getNickname()).isEqualTo("영희");
        assertThat(member.isRandomNickname()).isFalse();
    }
}

