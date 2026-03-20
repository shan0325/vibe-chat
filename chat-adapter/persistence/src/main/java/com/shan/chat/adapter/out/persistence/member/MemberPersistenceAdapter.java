package com.shan.chat.adapter.out.persistence.member;

import com.shan.chat.adapter.out.persistence.member.mapper.MemberMapper;
import com.shan.chat.adapter.out.persistence.member.repository.MemberJpaRepository;
import com.shan.chat.application.member.port.out.LoadMemberPort;
import com.shan.chat.application.member.port.out.SaveMemberPort;
import com.shan.chat.domain.member.MemberProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements SaveMemberPort, LoadMemberPort {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public void save(MemberProfile memberProfile) {
        memberJpaRepository.findById(memberProfile.getMemberId())
                .ifPresentOrElse(
                        entity -> entity.updateNickname(
                                memberProfile.getNickname(),
                                memberProfile.isRandomNickname()
                        ),
                        () -> memberJpaRepository.save(memberMapper.toNewEntity(memberProfile))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberProfile> loadByMemberId(String memberId) {
        return memberJpaRepository.findById(memberId).map(memberMapper::toDomain);
    }
}

