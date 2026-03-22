package com.shan.chat.application.lobby.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.in.GetLobbyHistoryUseCase;
import com.shan.chat.application.lobby.port.in.SendLobbyMessageUseCase;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.CreateMemberUseCase;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.application.room.port.out.BroadcastRoomPort;
import com.shan.chat.common.exception.SessionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * LobbyService 통합 테스트.
 * 실제 H2 DB + 실제 LobbyMessagePersistenceAdapter를 사용한다.
 * ManageOnlineSessionPort(OnlineSessionAdapter)도 실제 in-memory 빈을 사용한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class LobbyServiceIntegrationTest {

    @Autowired
    private SendLobbyMessageUseCase sendLobbyMessageUseCase;

    @Autowired
    private GetLobbyHistoryUseCase getLobbyHistoryUseCase;

    @Autowired
    private CreateMemberUseCase createMemberUseCase;

    // 실제 in-memory 세션 어댑터 — 세션을 직접 등록해 테스트한다
    @Autowired
    private ManageOnlineSessionPort manageOnlineSessionPort;

    // STOMP 브로드캐스트 격리 — 호출 검증에만 사용
    @MockitoBean
    private BroadcastLobbyPort broadcastLobbyPort;

    @MockitoBean
    private BroadcastRoomPort broadcastRoomPort;

    @Test
    @DisplayName("메시지 전송 시 DB에 저장되고 브로드캐스트가 호출된다")
    void send_savesToDbAndBroadcasts() {
        // 실제 멤버 생성 → 실제 세션 등록
        MemberInfo member = createMemberUseCase.create();
        manageOnlineSessionPort.addSession("session-1", member);

        sendLobbyMessageUseCase.send("session-1", "안녕하세요!");

        // 실제 DB에서 이력 조회
        List<LobbyMessageDto> history = getLobbyHistoryUseCase.getRecentHistory(50);
        assertThat(history).isNotEmpty();
        assertThat(history)
                .anyMatch(m -> m.getContent().equals("안녕하세요!")
                        && m.getSenderNickname().equals(member.getNickname()));

        // 브로드캐스트가 실제로 호출됐는지 검증
        verify(broadcastLobbyPort).broadcastMessage(any());
    }

    @Test
    @DisplayName("메시지 여러 건 전송 후 최근 이력 limit 수만큼만 반환된다")
    void send_multipleMessages_historyRespectedLimit() {
        MemberInfo member = createMemberUseCase.create();
        manageOnlineSessionPort.addSession("session-2", member);

        for (int i = 1; i <= 5; i++) {
            sendLobbyMessageUseCase.send("session-2", "메시지 " + i);
        }

        List<LobbyMessageDto> history = getLobbyHistoryUseCase.getRecentHistory(3);
        assertThat(history).hasSize(3);
    }

    @Test
    @DisplayName("존재하지 않는 세션으로 메시지 전송 시 SessionNotFoundException이 발생한다")
    void send_sessionNotFound_throwsException() {
        assertThatThrownBy(() -> sendLobbyMessageUseCase.send("no-session", "안녕"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    @DisplayName("메시지가 없을 때 이력 조회 시 빈 목록을 반환한다")
    void getRecentHistory_noMessages_returnsEmpty() {
        List<LobbyMessageDto> history = getLobbyHistoryUseCase.getRecentHistory(50);
        assertThat(history).isEmpty();
    }
}

