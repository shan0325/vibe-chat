package com.shan.chat.application.member.service;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.out.BroadcastLobbyPort;
import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.ConnectMemberUseCase;
import com.shan.chat.application.member.port.in.DisconnectMemberUseCase;
import com.shan.chat.application.member.port.in.GetOnlineMembersUseCase;
import com.shan.chat.application.member.port.in.SyncPresenceUseCase;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.ManageOnlineSessionPort;
import com.shan.chat.common.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PresenceService implements ConnectMemberUseCase, DisconnectMemberUseCase, GetOnlineMembersUseCase, SyncPresenceUseCase {

    /** 새로고침 등 순간적인 재연결을 퇴장으로 처리하지 않기 위한 유예 시간 (초) */
    private static final int DISCONNECT_GRACE_SECONDS = 3;

    private final ManageOnlineSessionPort manageOnlineSessionPort;
    private final BroadcastLobbyPort broadcastLobbyPort;
    private final LoadMemberPort loadMemberPort;

    /** memberId → 퇴장 예약 태스크 (grace period 동안 재연결이 오면 취소) */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();

    /** 데몬 스레드를 사용해 JVM 종료 시 자동으로 정리된다. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "presence-grace-scheduler");
        t.setDaemon(true);
        return t;
    });

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

        // grace period 중 재연결(새로고침 등) → 퇴장 취소, 입장 메시지 생략
        ScheduledFuture<?> pending = pendingDisconnects.remove(memberId);
        if (pending != null && pending.cancel(false)) {
            log.debug("[재연결] grace period 취소, nickname={}", memberInfo.getNickname());
            broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
            return;
        }

        // 신규 접속 — 입장 메시지 브로드캐스트
        broadcastLobbyPort.broadcastMessage(systemMsg(memberInfo.getNickname() + "님이 입장했습니다."));
        broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
    }

    @Override
    public void disconnect(String sessionId) {
        manageOnlineSessionPort.getMemberBySessionId(sessionId).ifPresent(memberInfo -> {
            manageOnlineSessionPort.removeSession(sessionId);
            log.info("[퇴장 예정] nickname={}, sessionId={}", memberInfo.getNickname(), sessionId);

            // 멀티탭: 같은 멤버의 다른 세션이 남아 있으면 퇴장 처리 불필요
            boolean stillOnline = manageOnlineSessionPort.getAllOnlineMembers().stream()
                    .anyMatch(m -> m.getMemberId().equals(memberInfo.getMemberId()));
            if (stillOnline) return;

            // grace period 후 재연결이 없으면 퇴장 확정
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                pendingDisconnects.remove(memberInfo.getMemberId());
                log.info("[퇴장 확정] nickname={}", memberInfo.getNickname());
                broadcastLobbyPort.broadcastMessage(systemMsg(memberInfo.getNickname() + "님이 퇴장했습니다."));
                broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
            }, DISCONNECT_GRACE_SECONDS, TimeUnit.SECONDS);

            pendingDisconnects.put(memberInfo.getMemberId(), task);
        });
    }

    @Override
    public String getAndDisconnect(String sessionId) {
        String memberId = manageOnlineSessionPort.getMemberBySessionId(sessionId)
                .map(MemberInfo::getMemberId)
                .orElse(null);
        disconnect(sessionId);
        return memberId;
    }

    @Override
    public List<MemberInfo> getOnlineMembers() {
        return manageOnlineSessionPort.getAllOnlineMembers();
    }

    @Override
    public void syncPresence() {
        broadcastLobbyPort.broadcastPresence(manageOnlineSessionPort.getAllOnlineMembers());
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
