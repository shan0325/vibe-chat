package com.shan.chat.application.lobby.port.out;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;

import java.util.List;

public interface LoadLobbyHistoryPort {
    List<LobbyMessageDto> loadRecent(int limit);
}

