package com.shan.chat.application.service;

import com.shan.chat.application.dto.MemberInfo;
import com.shan.chat.application.port.in.ChangeNicknameUseCase;
import com.shan.chat.application.port.in.CreateMemberUseCase;
import com.shan.chat.application.port.in.FindMemberUseCase;
import com.shan.chat.application.port.out.LoadMemberPort;
import com.shan.chat.application.port.out.SaveMemberPort;
import com.shan.chat.common.exception.ChatException;
import com.shan.chat.common.util.NicknameGenerator;
import com.shan.chat.domain.member.MemberProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements CreateMemberUseCase, ChangeNicknameUseCase, FindMemberUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;

    /** 랜덤 닉네임으로 신규 멤버 생성 후 저장 */
    @Override
    public MemberInfo create() {
        String memberId = UUID.randomUUID().toString();
        String randomNickname = NicknameGenerator.generate();
        MemberProfile member = MemberProfile.create(memberId, randomNickname);
        saveMemberPort.save(member);
        return toMemberInfo(member);
    }

    /** 닉네임 변경 — 도메인 규칙(20자 이하, 공백 불가) 검증 후 저장 */
    @Override
    public MemberInfo change(String memberId, String newNickname) {
        MemberProfile member = loadMemberPort.loadByMemberId(memberId)
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다: " + memberId));
        member.changeNickname(newNickname);
        saveMemberPort.save(member);
        return toMemberInfo(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberInfo> findByMemberId(String memberId) {
        return loadMemberPort.loadByMemberId(memberId).map(this::toMemberInfo);
    }

    private MemberInfo toMemberInfo(MemberProfile member) {
        return MemberInfo.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .randomNickname(member.isRandomNickname())
                .build();
    }
}

