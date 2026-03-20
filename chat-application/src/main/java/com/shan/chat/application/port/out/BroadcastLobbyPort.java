package com.shan.chat.application.port.out;

import com.shan.chat.application.dto.LobbyMessageDto;
import com.shan.chat.application.dto.MemberInfo;

import java.util.List;

/** 로비 메시지 · 접속자 목록 브로드캐스트 아웃바운드 포트 */
public interface BroadcastLobbyPort {
    void broadcastMessage(LobbyMessageDto message);
    void broadcastPresence(List<MemberInfo> onlineMembers);
}

