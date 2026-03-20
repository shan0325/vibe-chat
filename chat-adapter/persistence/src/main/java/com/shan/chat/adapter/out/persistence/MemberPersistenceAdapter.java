package com.shan.chat.adapter.out.persistence;

import com.shan.chat.adapter.out.persistence.mapper.MemberMapper;
import com.shan.chat.adapter.out.persistence.repository.MemberJpaRepository;
import com.shan.chat.application.port.out.LoadMemberPort;
import com.shan.chat.application.port.out.SaveMemberPort;
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

    /**
     * 저장 처리:
     * - 기존 엔티티가 있으면 닉네임만 업데이트 (더티 체킹)
     * - 없으면 신규 생성
     */
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
        return memberJpaRepository.findById(memberId)
                .map(memberMapper::toDomain);
    }
}

