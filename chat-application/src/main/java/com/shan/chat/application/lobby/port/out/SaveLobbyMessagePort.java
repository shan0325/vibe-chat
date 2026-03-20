package com.shan.chat.application.lobby.port.out;

import com.shan.chat.domain.message.ChatMessage;

public interface SaveLobbyMessagePort {
    void save(ChatMessage message);
}

