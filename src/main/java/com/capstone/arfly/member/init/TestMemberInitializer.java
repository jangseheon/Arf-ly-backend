package com.capstone.arfly.member.init;

import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestMemberInitializer implements ApplicationRunner {

    @Value("${app.user.id}")
    private String userId;

    @Value("${app.user.password}")
    private String userPassword;

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;


    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Member testMember1 = Member.builder().
                userId(userId)
                .password(passwordEncoder.encode(userPassword))
                .nickName("testUser").build();
        memberRepository.save(testMember1);
    }

}
