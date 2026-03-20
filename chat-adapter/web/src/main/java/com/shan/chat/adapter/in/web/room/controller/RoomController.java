package com.shan.chat.adapter.in.web.room.controller;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.FindMemberUseCase;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.dto.RoomMessageDto;
import com.shan.chat.application.room.port.in.*;
import com.shan.chat.common.exception.ChatException;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final CreateRoomUseCase createRoomUseCase;
    private final GetRoomListUseCase getRoomListUseCase;
    private final JoinRoomUseCase joinRoomUseCase;
    private final LeaveRoomUseCase leaveRoomUseCase;
    private final GetRoomHistoryUseCase getRoomHistoryUseCase;
    private final GetRoomParticipantsUseCase getRoomParticipantsUseCase;
    private final FindMemberUseCase findMemberUseCase;

    // ── 방 채팅 페이지 ─────────────────────────────────────────

    @GetMapping("/room/{roomId}")
    public String roomPage(@PathVariable("roomId") String roomId,
                           HttpSession session, Model model) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return "redirect:/";

        RoomDto room = getRoomListUseCase.getRoomById(roomId)
                .orElseThrow(() -> new ChatException("존재하지 않는 방입니다: " + roomId));

        MemberInfo memberInfo = findMemberUseCase.findByMemberId(memberId)
                .orElseThrow(() -> new ChatException("사용자 정보를 찾을 수 없습니다: " + memberId));

        // 방에 아직 참여하지 않은 경우 자동 입장
        joinRoomUseCase.join(memberId, roomId);

        model.addAttribute("room", room);
        model.addAttribute("memberInfo", memberInfo);
        return "room";
    }

    // ── REST API ──────────────────────────────────────────────

    @GetMapping("/api/rooms")
    @ResponseBody
    public List<RoomDto> getRoomList() {
        return getRoomListUseCase.getRoomList();
    }

    @PostMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomRequest request,
                                              HttpSession session) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return ResponseEntity.status(401).build();

        RoomDto room = createRoomUseCase.create(memberId, request.getRoomName());
        return ResponseEntity.ok(room);
    }

    @PostMapping("/api/rooms/{roomId}/leave")
    @ResponseBody
    public ResponseEntity<Void> leaveRoom(@PathVariable("roomId") String roomId,
                                          HttpSession session) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return ResponseEntity.status(401).build();

        leaveRoomUseCase.leave(memberId, roomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/rooms/{roomId}/history")
    @ResponseBody
    public List<RoomMessageDto> getRoomHistory(@PathVariable("roomId") String roomId,
                                               @RequestParam(defaultValue = "50") int limit) {
        return getRoomHistoryUseCase.getRecentHistory(roomId, Math.min(limit, 100));
    }

    @GetMapping("/api/rooms/{roomId}/participants")
    @ResponseBody
    public List<MemberInfo> getRoomParticipants(@PathVariable("roomId") String roomId) {
        return getRoomParticipantsUseCase.getParticipants(roomId);
    }

    // ── Inner DTO ─────────────────────────────────────────────

    @Getter
    @NoArgsConstructor
    public static class CreateRoomRequest {
        private String roomName;
    }
}

