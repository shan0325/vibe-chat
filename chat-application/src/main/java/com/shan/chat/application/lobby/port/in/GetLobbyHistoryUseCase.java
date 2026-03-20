package com.shan.chat.application.lobby.port.in;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;

import java.util.List;

public interface GetLobbyHistoryUseCase {
    List<LobbyMessageDto> getRecentHistory(int limit);
}

