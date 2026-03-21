package com.shan.chat.application.member.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.SaveMemberPort;
import com.shan.chat.common.exception.MemberNotFoundException;
import com.shan.chat.domain.member.MemberProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("멤버 생성 시 UUID 기반 memberId와 랜덤 닉네임이 할당된다")
    void create_returnsNewMember() {
        MemberInfo result = memberService.create();

        assertThat(result.getMemberId()).isNotBlank();
        assertThat(result.getNickname()).isNotBlank();
        assertThat(result.isRandomNickname()).isTrue();

        ArgumentCaptor<MemberProfile> captor = ArgumentCaptor.forClass(MemberProfile.class);
        then(saveMemberPort).should().save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(result.getMemberId());
    }

    @Test
    @DisplayName("닉네임 변경 시 변경된 닉네임으로 저장된다")
    void change_updatesNickname() {
        MemberProfile member = MemberProfile.create("member-1", "귀여운고양이");
        given(loadMemberPort.loadByMemberId("member-1")).willReturn(Optional.of(member));

        MemberInfo result = memberService.change("member-1", "철수");

        assertThat(result.getNickname()).isEqualTo("철수");
        assertThat(result.isRandomNickname()).isFalse();
        then(saveMemberPort).should().save(member);
    }

    @Test
    @DisplayName("존재하지 않는 멤버의 닉네임 변경 시 예외가 발생한다")
    void change_memberNotFound_throwsException() {
        given(loadMemberPort.loadByMemberId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.change("unknown", "새닉네임"))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("멤버 조회 시 존재하면 Optional로 반환된다")
    void findByMemberId_exists_returnsOptional() {
        MemberProfile member = MemberProfile.restore("member-1", "철수", false);
        given(loadMemberPort.loadByMemberId("member-1")).willReturn(Optional.of(member));

        Optional<MemberInfo> result = memberService.findByMemberId("member-1");

        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo("철수");
    }

    @Test
    @DisplayName("존재하지 않는 멤버 조회 시 Optional.empty()를 반환한다")
    void findByMemberId_notExists_returnsEmpty() {
        given(loadMemberPort.loadByMemberId("none")).willReturn(Optional.empty());

        Optional<MemberInfo> result = memberService.findByMemberId("none");

        assertThat(result).isEmpty();
    }
}

