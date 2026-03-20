package com.shan.chat.adapter.in.web.lobby.controller;

import com.shan.chat.application.lobby.dto.LobbyMessageDto;
import com.shan.chat.application.lobby.port.in.GetLobbyHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lobby")
@RequiredArgsConstructor
public class LobbyHistoryController {

    private final GetLobbyHistoryUseCase getLobbyHistoryUseCase;

    @GetMapping("/history")
    public List<LobbyMessageDto> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return getLobbyHistoryUseCase.getRecentHistory(Math.min(limit, 100));
    }
}

