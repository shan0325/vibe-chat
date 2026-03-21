package com.shan.chat.adapter.in.web.room.controller;

import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.port.in.StartDirectChatUseCase;
import com.shan.chat.common.exception.ChatException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/direct")
@RequiredArgsConstructor
public class DirectChatController {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final StartDirectChatUseCase startDirectChatUseCase;

    /** 1:1 채팅 시작 - 기존 방을 찾거나 새 방을 생성해 반환 */
    @PostMapping("/{targetMemberId}")
    public ResponseEntity<RoomDto> startDirectChat(
            @PathVariable("targetMemberId") String targetMemberId,
            HttpSession session) {

        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return ResponseEntity.status(401).build();
        if (memberId.equals(targetMemberId)) {
            throw new ChatException("자기 자신에게 1:1 채팅을 시작할 수 없습니다.");
        }

        RoomDto room = startDirectChatUseCase.startDirectChat(memberId, targetMemberId);
        return ResponseEntity.ok(room);
    }

    /** 내가 참여 중인 1:1 대화 목록 */
    @GetMapping("/rooms")
    public List<RoomDto> getMyDirectRooms(HttpSession session) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return List.of();
        return startDirectChatUseCase.getMyDirectRooms(memberId);
    }
}

