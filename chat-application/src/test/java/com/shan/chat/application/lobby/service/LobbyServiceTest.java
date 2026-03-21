package com.shan.chat.application.lobby.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.lobby.port.out.LoadLobbyHistoryPort;
import com.shan.chat.application.lobby.port.out.SaveLobbyMessagePort;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.common.exception.SessionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private ManageOnlineSessionPort manageOnlineSessionPort;

    @Mock
    private BroadcastLobbyPort broadcastLobbyPort;

    @Mock
    private SaveLobbyMessagePort saveLobbyMessagePort;

    @Mock
    private LoadLobbyHistoryPort loadLobbyHistoryPort;

    @InjectMocks
    private LobbyService lobbyService;

    @Test
    @DisplayName("유효한 세션으로 메시지 전송 시 저장 및 브로드캐스트된다")
    void send_validSession_savesAndBroadcasts() {
        MemberInfo sender = MemberInfo.builder()
                .memberId("member-1").nickname("철수").randomNickname(false).build();
        given(manageOnlineSessionPort.getMemberBySessionId("session-1"))
                .willReturn(Optional.of(sender));

        lobbyService.send("session-1", "안녕하세요!");

        then(saveLobbyMessagePort).should().save(any());

        ArgumentCaptor<LobbyMessageDto> captor = ArgumentCaptor.forClass(LobbyMessageDto.class);
        then(broadcastLobbyPort).should().broadcastMessage(captor.capture());

        LobbyMessageDto broadcast = captor.getValue();
        assertThat(broadcast.getSenderNickname()).isEqualTo("철수");
        assertThat(broadcast.getContent()).isEqualTo("안녕하세요!");
        assertThat(broadcast.getType()).isEqualTo(LobbyMessageDto.MessageType.TEXT);
    }

    @Test
    @DisplayName("존재하지 않는 세션으로 메시지 전송 시 예외가 발생한다")
    void send_sessionNotFound_throwsException() {
        given(manageOnlineSessionPort.getMemberBySessionId("unknown"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> lobbyService.send("unknown", "안녕"))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("세션을 찾을 수 없습니다");

        then(saveLobbyMessagePort).shouldHaveNoInteractions();
        then(broadcastLobbyPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("최근 로비 이력 조회 시 포트에서 반환된 결과를 그대로 반환한다")
    void getRecentHistory_delegatesToPort() {
        List<LobbyMessageDto> expected = List.of(
                LobbyMessageDto.builder().content("메시지1").build(),
                LobbyMessageDto.builder().content("메시지2").build()
        );
        given(loadLobbyHistoryPort.loadRecent(50)).willReturn(expected);

        List<LobbyMessageDto> result = lobbyService.getRecentHistory(50);

        assertThat(result).isEqualTo(expected);
        then(loadLobbyHistoryPort).should().loadRecent(50);
    }
}

