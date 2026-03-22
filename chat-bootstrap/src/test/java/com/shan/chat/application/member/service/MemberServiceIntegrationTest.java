package com.shan.chat.application.member.service;

import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.ChangeNicknameUseCase;
import com.shan.chat.application.member.port.in.CreateMemberUseCase;
import com.shan.chat.application.member.port.in.FindMemberUseCase;
import com.shan.chat.application.room.port.out.BroadcastRoomPort;
import com.shan.chat.common.exception.MemberNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * MemberService 통합 테스트.
 * 실제 H2 DB + 실제 MemberPersistenceAdapter를 사용한다.
 * WebSocket 브로드캐스트 포트는 인프라 의존이 없어도 돼서 MockBean으로 격리한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MemberServiceIntegrationTest {

    @Autowired
    private CreateMemberUseCase createMemberUseCase;

    @Autowired
    private ChangeNicknameUseCase changeNicknameUseCase;

    @Autowired
    private FindMemberUseCase findMemberUseCase;

    // STOMP 브로드캐스트 — WebSocket 인프라 없이는 의미 없으므로 격리
    @MockitoBean
    private BroadcastLobbyPort broadcastLobbyPort;

    @MockitoBean
    private BroadcastRoomPort broadcastRoomPort;

    @Test
    @DisplayName("멤버 생성 후 DB에서 조회할 수 있다")
    void create_thenFindFromDb() {
        MemberInfo created = createMemberUseCase.create();

        Optional<MemberInfo> found = findMemberUseCase.findByMemberId(created.getMemberId());

        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(created.getMemberId());
        assertThat(found.get().getNickname()).isEqualTo(created.getNickname());
        assertThat(found.get().isRandomNickname()).isTrue();
    }

    @Test
    @DisplayName("닉네임 변경 후 DB에서 변경된 값이 조회된다")
    void change_persistsNicknameToDb() {
        MemberInfo created = createMemberUseCase.create();

        changeNicknameUseCase.change(created.getMemberId(), "새닉네임");

        MemberInfo updated = findMemberUseCase.findByMemberId(created.getMemberId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("새닉네임");
        assertThat(updated.isRandomNickname()).isFalse();
    }

    @Test
    @DisplayName("닉네임 변경은 다른 멤버에게 영향을 주지 않는다")
    void change_doesNotAffectOtherMembers() {
        MemberInfo member1 = createMemberUseCase.create();
        MemberInfo member2 = createMemberUseCase.create();

        changeNicknameUseCase.change(member1.getMemberId(), "변경닉네임");

        MemberInfo found2 = findMemberUseCase.findByMemberId(member2.getMemberId()).orElseThrow();
        assertThat(found2.getNickname()).isEqualTo(member2.getNickname());
    }

    @Test
    @DisplayName("존재하지 않는 멤버 닉네임 변경 시 MemberNotFoundException이 발생한다")
    void change_notFound_throwsMemberNotFoundException() {
        assertThatThrownBy(() -> changeNicknameUseCase.change("없는-id", "닉네임"))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 멤버 조회 시 Optional.empty()를 반환한다")
    void find_notExists_returnsEmpty() {
        Optional<MemberInfo> result = findMemberUseCase.findByMemberId("없는-id");
        assertThat(result).isEmpty();
    }
}

