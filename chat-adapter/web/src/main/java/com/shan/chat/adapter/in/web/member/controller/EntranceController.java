package com.shan.chat.adapter.in.web.member.controller;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.ChangeNicknameUseCase;
import com.shan.chat.application.member.port.in.CreateMemberUseCase;
import com.shan.chat.application.member.port.in.FindMemberUseCase;
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

    @PostMapping("/nickname")
    public String setNickname(@RequestParam("nickname") String nickname, HttpSession session) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return "redirect:/";

        String trimmed = nickname.trim();
        if (trimmed.isEmpty()) return "redirect:/";   // 빈 닉네임은 입구 페이지로

        changeNicknameUseCase.change(memberId, trimmed);
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String chatMain(HttpSession session, Model model) {
        String memberId = (String) session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) return "redirect:/";
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

