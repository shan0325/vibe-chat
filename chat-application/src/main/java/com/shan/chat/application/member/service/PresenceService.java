package com.shan.chat.application.member.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.ConnectMemberUseCase;
import com.shan.chat.application.member.port.in.DisconnectMemberUseCase;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.common.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService implements ConnectMemberUseCase, DisconnectMemberUseCase {

    private final ManageOnlineSessionPort manageOnlineSessionPort;
    private final BroadcastLobbyPort broadcastLobbyPort;
    private final LoadMemberPort loadMemberPort;

    @Override
    public void connect(String sessionId, String memberId) {
        MemberInfo memberInfo = loadMemberPort.loadByMemberId(memberId)
                .map(m -> MemberInfo.builder()
                        .memberId(m.getMemberId())
                        .nickname(m.getNickname())
                        .randomNickname(m.isRandomNickname())
                        .build())
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다: " + memberId));

        manageOnlineSessionPort.addSession(sessionId, memberInfo);
        log.info("[접속] nickname={}, sessionId={}", memberInfo.getNickname(), sessionId);

        broadcastLobbyPort.broadcastMessage(systemMsg(memberInfo.getNickname() + "님이 입장했습니다."));
        broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
    }

    @Override
    public void disconnect(String sessionId) {
        manageOnlineSessionPort.getMemberBySessionId(sessionId).ifPresent(memberInfo -> {
            manageOnlineSessionPort.removeSession(sessionId);
            log.info("[퇴장] nickname={}, sessionId={}", memberInfo.getNickname(), sessionId);

            broadcastLobbyPort.broadcastMessage(systemMsg(memberInfo.getNickname() + "님이 퇴장했습니다."));
            broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
        });
    }

    private LobbyMessageDto systemMsg(String content) {
        return LobbyMessageDto.builder()
                .content(content)
                .sentAt(nowHHmm())
                .type(LobbyMessageDto.MessageType.SYSTEM)
                .build();
    }

    private String nowHHmm() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }
}

