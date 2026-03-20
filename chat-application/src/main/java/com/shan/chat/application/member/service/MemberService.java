package com.shan.chat.application.member.service;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.member.port.in.ChangeNicknameUseCase;
import com.shan.chat.application.member.port.in.CreateMemberUseCase;
import com.shan.chat.application.member.port.in.FindMemberUseCase;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.SaveMemberPort;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.common.util.NicknameGenerator;
import com.shan.chat.domain.member.MemberProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements CreateMemberUseCase, ChangeNicknameUseCase, FindMemberUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;

    @Override
    @Transactional
    public MemberInfo create() {
        String memberId = UUID.randomUUID().toString();
        MemberProfile member = MemberProfile.create(memberId, NicknameGenerator.generate());
        saveMemberPort.save(member);
        return toDto(member);
    }

    @Override
    @Transactional
    public MemberInfo change(String memberId, String newNickname) {
        MemberProfile member = loadMemberPort.loadByMemberId(memberId)
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다: " + memberId));
        member.changeNickname(newNickname);
        saveMemberPort.save(member);
        return toDto(member);
    }

    @Override
    public Optional<MemberInfo> findByMemberId(String memberId) {
        return loadMemberPort.loadByMemberId(memberId).map(this::toDto);
    }

    private MemberInfo toDto(MemberProfile m) {
        return MemberInfo.builder()
                .memberId(m.getMemberId())
                .nickname(m.getNickname())
                .randomNickname(m.isRandomNickname())
                .build();
    }
}

