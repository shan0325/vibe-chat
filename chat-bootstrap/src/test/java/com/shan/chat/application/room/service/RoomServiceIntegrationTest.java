package com.shan.chat.application.room.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.CreateMemberUseCase;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.port.in.*;
import com.shan.chat.application.room.port.out.BroadcastRoomPort;
import com.shan.chat.common.exception.RoomNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * RoomService 통합 테스트.
 * 실제 H2 DB + 실제 RoomPersistenceAdapter를 사용한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RoomServiceIntegrationTest {

    @Autowired
    private CreateMemberUseCase createMemberUseCase;

    @Autowired
    private CreateRoomUseCase createRoomUseCase;

    @Autowired
    private JoinRoomUseCase joinRoomUseCase;

    @Autowired
    private LeaveRoomUseCase leaveRoomUseCase;

    @Autowired
    private GetRoomListUseCase getRoomListUseCase;

    @Autowired
    private GetRoomParticipantsUseCase getRoomParticipantsUseCase;

    @Autowired
    private SearchRoomUseCase searchRoomUseCase;

    // STOMP 브로드캐스트 격리
    @MockitoBean
    private BroadcastRoomPort broadcastRoomPort;

    // ── 방 생성 ───────────────────────────────────────────────

    @Test
    @DisplayName("방 생성 후 방 목록에서 조회된다")
    void create_roomAppearsInList() {
        MemberInfo member = createMemberUseCase.create();

        RoomDto room = createRoomUseCase.create(member.getMemberId(), "스터디방");

        List<RoomDto> list = getRoomListUseCase.getRoomList();
        assertThat(list).extracting(RoomDto::getRoomId).contains(room.getRoomId());
        assertThat(list).extracting(RoomDto::getRoomName).contains("스터디방");
    }

    @Test
    @DisplayName("방 생성 시 생성자가 참여자에 자동 포함된다")
    void create_creatorIsAutoJoined() {
        MemberInfo member = createMemberUseCase.create();

        RoomDto room = createRoomUseCase.create(member.getMemberId(), "스터디방");

        List<MemberInfo> participants = getRoomParticipantsUseCase.getParticipants(room.getRoomId());
        assertThat(participants)
                .extracting(MemberInfo::getMemberId)
                .contains(member.getMemberId());
    }

    @Test
    @DisplayName("방 생성 후 브로드캐스트가 호출된다")
    void create_broadcastsCalled() {
        MemberInfo member = createMemberUseCase.create();

        createRoomUseCase.create(member.getMemberId(), "스터디방");

        verify(broadcastRoomPort).broadcastRoomList(any());
    }

    // ── 방 입장 ───────────────────────────────────────────────

    @Test
    @DisplayName("다른 멤버가 방에 입장하면 참여자 수가 증가한다")
    void join_increasesParticipantCount() {
        MemberInfo creator = createMemberUseCase.create();
        MemberInfo joiner  = createMemberUseCase.create();
        RoomDto room = createRoomUseCase.create(creator.getMemberId(), "스터디방");

        joinRoomUseCase.join(joiner.getMemberId(), room.getRoomId());

        List<MemberInfo> participants = getRoomParticipantsUseCase.getParticipants(room.getRoomId());
        assertThat(participants).hasSize(2)
                .extracting(MemberInfo::getMemberId)
                .containsExactlyInAnyOrder(creator.getMemberId(), joiner.getMemberId());
    }

    @Test
    @DisplayName("이미 입장한 멤버가 다시 입장해도 참여자 수가 변하지 않는다")
    void join_duplicateJoin_noChange() {
        MemberInfo member = createMemberUseCase.create();
        RoomDto room = createRoomUseCase.create(member.getMemberId(), "스터디방");

        joinRoomUseCase.join(member.getMemberId(), room.getRoomId());  // 중복 입장

        List<MemberInfo> participants = getRoomParticipantsUseCase.getParticipants(room.getRoomId());
        assertThat(participants).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 방에 입장 시 RoomNotFoundException이 발생한다")
    void join_roomNotFound_throwsException() {
        MemberInfo member = createMemberUseCase.create();

        assertThatThrownBy(() -> joinRoomUseCase.join(member.getMemberId(), "없는-방"))
                .isInstanceOf(RoomNotFoundException.class);
    }

    // ── 방 퇴장 ───────────────────────────────────────────────

    @Test
    @DisplayName("멤버가 방에서 퇴장하면 참여자 목록에서 제거된다")
    void leave_removedFromParticipants() {
        MemberInfo creator = createMemberUseCase.create();
        MemberInfo joiner  = createMemberUseCase.create();
        RoomDto room = createRoomUseCase.create(creator.getMemberId(), "스터디방");
        joinRoomUseCase.join(joiner.getMemberId(), room.getRoomId());

        leaveRoomUseCase.leave(joiner.getMemberId(), room.getRoomId());

        List<MemberInfo> participants = getRoomParticipantsUseCase.getParticipants(room.getRoomId());
        assertThat(participants)
                .extracting(MemberInfo::getMemberId)
                .doesNotContain(joiner.getMemberId());
    }

    // ── 방 검색 ───────────────────────────────────────────────

    @Test
    @DisplayName("키워드로 방 이름을 검색할 수 있다")
    void searchRooms_byKeyword() {
        MemberInfo member = createMemberUseCase.create();
        createRoomUseCase.create(member.getMemberId(), "자바 스터디");
        createRoomUseCase.create(member.getMemberId(), "파이썬 모임");

        List<RoomDto> result = searchRoomUseCase.searchRooms("자바");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomName()).isEqualTo("자바 스터디");
    }

    @Test
    @DisplayName("키워드 없이 검색 시 전체 GROUP 방 목록을 반환한다")
    void searchRooms_noKeyword_returnsAll() {
        MemberInfo member = createMemberUseCase.create();
        createRoomUseCase.create(member.getMemberId(), "방1");
        createRoomUseCase.create(member.getMemberId(), "방2");

        List<RoomDto> result = searchRoomUseCase.searchRooms(null);

        assertThat(result)
                .extracting(RoomDto::getRoomName)
                .contains("방1", "방2");
    }
}

