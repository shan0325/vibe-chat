package com.shan.chat.application.room.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.port.out.*;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.common.exception.RoomNotFoundException;
import com.shan.chat.domain.member.MemberProfile;
import com.shan.chat.domain.room.ChatRoom;
import com.shan.chat.domain.room.RoomParticipant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private LoadRoomPort loadRoomPort;
    @Mock private SaveRoomPort saveRoomPort;
    @Mock private LoadRoomParticipantPort loadRoomParticipantPort;
    @Mock private SaveRoomParticipantPort saveRoomParticipantPort;
    @Mock private BroadcastRoomPort broadcastRoomPort;
    @Mock private LoadMemberPort loadMemberPort;
    @Mock private SaveRoomMessagePort saveRoomMessagePort;
    @Mock private FindDirectRoomPort findDirectRoomPort;
    @Mock private ManageOnlineSessionPort manageOnlineSessionPort;
    @Mock private SearchRoomPort searchRoomPort;

    @InjectMocks
    private RoomService roomService;

    // ── 방 생성 ───────────────────────────────────────────────

    @Test
    @DisplayName("방 생성 시 방이 저장되고 생성자가 자동 입장된다")
    void create_savesRoomAndJoinsCreator() {
        given(loadMemberPort.loadByMemberId("member-1"))
                .willReturn(Optional.of(MemberProfile.restore("member-1", "철수", false)));
        given(loadRoomParticipantPort.countByRoomId(anyString())).willReturn(1);

        RoomDto result = roomService.create("member-1", "스터디방");

        assertThat(result.getRoomName()).isEqualTo("스터디방");
        assertThat(result.getRoomType()).isEqualTo("GROUP");
        then(saveRoomPort).should().save(any(ChatRoom.class));
        then(saveRoomParticipantPort).should().save(any(RoomParticipant.class));
    }

    @Test
    @DisplayName("방 이름이 빈 값이면 예외가 발생한다")
    void create_blankName_throwsException() {
        assertThatThrownBy(() -> roomService.create("member-1", "  "))
                .isInstanceOf(ChatException.class);
    }

    // ── 방 입장 ───────────────────────────────────────────────

    @Test
    @DisplayName("신규 참여자가 GROUP 방에 입장하면 참여자로 저장된다")
    void join_newMember_savesParticipant() {
        ChatRoom room = ChatRoom.restore("room-1", "스터디방", "creator", LocalDateTime.now(), true, ChatRoom.RoomType.GROUP);
        given(loadRoomPort.loadById("room-1")).willReturn(Optional.of(room));
        given(loadRoomParticipantPort.existsByRoomIdAndMemberId("room-1", "member-2")).willReturn(false);
        given(loadMemberPort.loadByMemberId("member-2"))
                .willReturn(Optional.of(MemberProfile.restore("member-2", "영희", false)));
        given(loadRoomParticipantPort.loadParticipantsWithInfoByRoomId("room-1"))
                .willReturn(List.of());

        roomService.join("member-2", "room-1");

        then(saveRoomParticipantPort).should().save(any(RoomParticipant.class));
    }

    @Test
    @DisplayName("이미 입장한 참여자는 중복 저장되지 않는다")
    void join_existingMember_doesNotSaveAgain() {
        ChatRoom room = ChatRoom.restore("room-1", "스터디방", "creator", LocalDateTime.now(), true, ChatRoom.RoomType.GROUP);
        given(loadRoomPort.loadById("room-1")).willReturn(Optional.of(room));
        given(loadRoomParticipantPort.existsByRoomIdAndMemberId("room-1", "member-1")).willReturn(true);
        given(loadRoomParticipantPort.loadParticipantsWithInfoByRoomId("room-1")).willReturn(List.of());

        roomService.join("member-1", "room-1");

        then(saveRoomParticipantPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비활성화된 방에 입장 시 예외가 발생한다")
    void join_inactiveRoom_throwsException() {
        ChatRoom inactiveRoom = ChatRoom.restore("room-1", "종료된방", "creator", LocalDateTime.now(), false, ChatRoom.RoomType.GROUP);
        given(loadRoomPort.loadById("room-1")).willReturn(Optional.of(inactiveRoom));

        assertThatThrownBy(() -> roomService.join("member-1", "room-1"))
                .isInstanceOf(ChatException.class)
                .hasMessageContaining("비활성화");
    }

    @Test
    @DisplayName("존재하지 않는 방에 입장 시 예외가 발생한다")
    void join_roomNotFound_throwsException() {
        given(loadRoomPort.loadById("no-room")).willReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.join("member-1", "no-room"))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("존재하지 않는 방");
    }

    // ── 방 퇴장 ───────────────────────────────────────────────

    @Test
    @DisplayName("참여 중인 방에서 퇴장 시 참여자가 삭제된다")
    void leave_removesParticipant() {
        ChatRoom room = ChatRoom.restore("room-1", "스터디방", "creator", LocalDateTime.now(), true, ChatRoom.RoomType.GROUP);
        given(loadRoomParticipantPort.existsByRoomIdAndMemberId("room-1", "member-1")).willReturn(true);
        given(loadRoomPort.loadById("room-1")).willReturn(Optional.of(room));
        given(loadMemberPort.loadByMemberId("member-1"))
                .willReturn(Optional.of(MemberProfile.restore("member-1", "철수", false)));
        given(loadRoomParticipantPort.loadParticipantsWithInfoByRoomId("room-1")).willReturn(List.of());

        roomService.leave("member-1", "room-1");

        then(saveRoomParticipantPort).should().deleteByRoomIdAndMemberId("room-1", "member-1");
    }

    @Test
    @DisplayName("참여 중이지 않은 방에서 퇴장 호출 시 아무 처리도 하지 않는다")
    void leave_notParticipant_doesNothing() {
        given(loadRoomParticipantPort.existsByRoomIdAndMemberId("room-1", "member-1")).willReturn(false);

        roomService.leave("member-1", "room-1");

        then(saveRoomParticipantPort).shouldHaveNoInteractions();
    }

    // ── 방 검색 ───────────────────────────────────────────────

    @Test
    @DisplayName("키워드 검색 시 SearchRoomPort에 위임하고 결과를 반환한다")
    void searchRooms_delegatesToPort() {
        ChatRoom room = ChatRoom.restore("room-1", "스터디방", "creator", LocalDateTime.now(), true, ChatRoom.RoomType.GROUP);
        given(searchRoomPort.searchGroupRooms("스터디")).willReturn(List.of(room));
        given(loadRoomParticipantPort.countByRoomId("room-1")).willReturn(3);

        List<RoomDto> result = roomService.searchRooms("스터디");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomName()).isEqualTo("스터디방");
        assertThat(result.get(0).getParticipantCount()).isEqualTo(3);
    }

    // ── 방 목록 ───────────────────────────────────────────────

    @Test
    @DisplayName("방 목록 조회 시 GROUP 방만 반환된다")
    void getRoomList_returnsOnlyGroupRooms() {
        ChatRoom groupRoom  = ChatRoom.restore("r1", "공개방", "c1", LocalDateTime.now(), true, ChatRoom.RoomType.GROUP);
        ChatRoom directRoom = ChatRoom.restore("r2", "1:1방",  "c1", LocalDateTime.now(), true, ChatRoom.RoomType.DIRECT);
        given(loadRoomPort.loadAllActive()).willReturn(List.of(groupRoom, directRoom));
        given(loadRoomParticipantPort.countByRoomId(anyString())).willReturn(1);

        List<RoomDto> result = roomService.getRoomList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomType()).isEqualTo("GROUP");
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private MemberInfo memberInfo(String memberId, String nickname) {
        return MemberInfo.builder().memberId(memberId).nickname(nickname).randomNickname(false).build();
    }
}

