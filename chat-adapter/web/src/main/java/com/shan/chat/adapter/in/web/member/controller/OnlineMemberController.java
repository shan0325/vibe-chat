package com.shan.chat.adapter.in.web.member.controller;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.GetOnlineMembersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class OnlineMemberController {

    private final GetOnlineMembersUseCase getOnlineMembersUseCase;

    @GetMapping("/online")
    public List<MemberInfo> getOnlineMembers() {
        return getOnlineMembersUseCase.getOnlineMembers();
    }
}

