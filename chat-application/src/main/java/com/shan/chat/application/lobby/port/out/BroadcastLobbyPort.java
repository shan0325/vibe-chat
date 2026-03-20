package com.shan.chat.application.lobby.port.out;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.member.dto.MemberInfo;

import java.util.List;

public interface BroadcastLobbyPort {
    void broadcastMessage(LobbyMessageDto message);
    void broadcastPresence(List<MemberInfo> onlineMembers);
}

