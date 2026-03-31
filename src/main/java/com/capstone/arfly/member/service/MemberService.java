package com.capstone.arfly.member.service;

import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.dto.UserNameCheckRequestDto;
import com.capstone.arfly.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public boolean isUsernameAvailable(UserNameCheckRequestDto userNameCheckRequestDto) {
        Optional<Member> member = memberRepository.findByNickName(userNameCheckRequestDto.getNickname());
        if(member.isPresent()){
            return false;
        }
        return true;
    }
}
