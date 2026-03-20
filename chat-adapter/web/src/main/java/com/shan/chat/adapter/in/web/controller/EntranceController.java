package com.shan.chat.adapter.in.web.controller;

import com.shan.chat.application.dto.MemberInfo;
import com.shan.chat.application.port.in.ChangeNicknameUseCase;
import com.shan.chat.application.port.in.CreateMemberUseCase;
import com.shan.chat.application.port.in.FindMemberUseCase;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EntranceController {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final CreateMemberUseCase createMemberUseCase;
    private final ChangeNicknameUseCase changeNicknameUseCase;
    private final FindMemberUseCase findMemberUseCase;

    /**
     * 진입 페이지
     * - 세션에 memberId 없음 → 신규 멤버 생성 후 랜덤 닉네임 노출
     * - 세션에 memberId 있음 → 기존 멤버 조회 (DB에 없으면 재생성)
     */
    @GetMapping("/")
    public String entrance(HttpSession session, Model model) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);

        MemberInfo memberInfo;
        if (memberId != null) {
            memberInfo = findMemberUseCase.findByMemberId(memberId)
                    .orElseGet(() -> createAndStoreNew(session));
        } else {
            memberInfo = createAndStoreNew(session);
        }

        model.addAttribute("memberInfo", memberInfo);
        return "entrance";
    }

    /**
     * 닉네임 설정 처리
     * 도메인 규칙 검증(20자 이하, 공백 불가) 후 메인 페이지로 이동
     */
    @PostMapping("/nickname")
    public String setNickname(@RequestParam("nickname") String nickname, HttpSession session) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) {
            return "redirect:/";
        }
        changeNicknameUseCase.change(memberId, nickname.trim());
        return "redirect:/main";
    }

    /**
     * 메인 페이지
     * Phase 2에서 전체 채팅 기능 추가 예정
     */
    @GetMapping("/main")
    public String main(HttpSession session, Model model) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) {
            return "redirect:/";
        }
        MemberInfo memberInfo = findMemberUseCase.findByMemberId(memberId)
                .orElseGet(() -> createAndStoreNew(session));
        model.addAttribute("memberInfo", memberInfo);
        return "main";
    }

    private MemberInfo createAndStoreNew(HttpSession session) {
        MemberInfo created = createMemberUseCase.create();
        session.setAttribute(SESSION_MEMBER_ID, created.getMemberId());
        return created;
    }
}

