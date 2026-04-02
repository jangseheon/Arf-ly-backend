package com.capstone.arfly.member.service;

import com.capstone.arfly.common.auth.JwtTokenUtil;
import com.capstone.arfly.common.exception.InvalidCredentialsException;
import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.PhoneAlreadyException;
import com.capstone.arfly.common.exception.UserAlreadyExistsException;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.domain.RefreshToken;
import com.capstone.arfly.member.domain.SocialType;
import com.capstone.arfly.member.domain.Terms;
import com.capstone.arfly.member.domain.UserTermsAgreement;
import com.capstone.arfly.member.dto.AccessTokenRequestDto;
import com.capstone.arfly.member.dto.LogoutRequestDto;
import com.capstone.arfly.member.dto.MemberCreateDto;
import com.capstone.arfly.member.dto.MemberLoginDto;
import com.capstone.arfly.member.dto.PhoneAuthInfoDto;
import com.capstone.arfly.member.dto.TokenResponseDto;
import com.capstone.arfly.member.dto.UserAgreementDto;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.member.repository.RefreshTokenRepository;
import com.capstone.arfly.member.repository.TermsRepository;
import com.capstone.arfly.member.repository.UserTermsAgreementRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public Member create(MemberCreateDto memberCreateDto) {
        //중복 체크
        memberRepository.findByUserId(memberCreateDto.getUserId()).ifPresent(m -> {
            throw new UserAlreadyExistsException();
        });

        //토큰 정보 추출
        String firebaseUid = memberCreateDto.getToken().getTokenId();
        String phoneNumber = memberCreateDto.getToken().getPhoneNumber();

        // 새로운 멤버 생성
        Member member = Member.builder()
                .userId(memberCreateDto.getUserId())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .firebaseUid(firebaseUid)
                .phoneNumber(phoneNumber)
                .build();
        memberRepository.save(member);

        // 약관 동의 처리
        List<UserAgreementDto> userAgreementDtoList = memberCreateDto.getUserAgreements();

        if (userAgreementDtoList != null && !userAgreementDtoList.isEmpty()) {
            // Id 리스트 추출
            List<Long> agreementList = userAgreementDtoList.stream()
                    .map(UserAgreementDto::getTermId)
                    .toList();

            // DB에서 가져온 약관을 Map 형태로 변환 (Key: 약관 ID, Value: 약관 Entity)
            Map<Long, Terms> termsMap = termsRepository.findByIdIn(agreementList).stream()
                    .collect(Collectors.toMap(Terms::getId, Function.identity()));

            // 클라이언트가 보낸 약관 ID가 DB에 전부 존재하는지 검증
            if (termsMap.size() != agreementList.size()) {
                throw new IllegalArgumentException("유효하지 않은 약관 ID가 포함되어 있습니다.");
            }

            // 약관 동의 엔티티 생성
            List<UserTermsAgreement> userTermsAgreementList = userAgreementDtoList.stream()
                    .map(dto -> UserTermsAgreement.builder()
                            .member(member)
                            .terms(termsMap.get(dto.getTermId()))
                            .agreement(dto.getTermsOfServiceAgreed())
                            .build())
                    .toList();

            // 동의 약관 저장
            userTermsAgreementRepository.saveAll(userTermsAgreementList);
        }
        return member;
    }

    //엑세스 및 리프레시 토큰 생성 및 반환
    public TokenResponseDto generateTokens(Member member) {
        String accessToken = jwtTokenUtil.createAccessToken(member.getId(), member.getRole().toString());
        String refreshToken = jwtTokenUtil.createRefreshToken(member);
        TokenResponseDto response = TokenResponseDto.builder().accessToken(accessToken)
                .refreshToken(refreshToken).build();
        return response;
    }

    public Member login(MemberLoginDto memberLoginDto) {
        //ID 확인
        Optional<Member> optMember = memberRepository.findByUserId(memberLoginDto.getUserId());
        if (optMember.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        Member member = optMember.get();

        //패스워드 검증
        if (!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return member;
    }


    public Member validateRefreshToken(AccessTokenRequestDto accessTokenRequestDto) {
        //토큰 유효성 검증
        jwtTokenUtil.validateRefreshToken(accessTokenRequestDto.getRefreshToken());
        Optional<RefreshToken> optToken = refreshTokenRepository.findByToken(accessTokenRequestDto.getRefreshToken());
        if (optToken.isEmpty()) {
            throw new InvalidTokenException();
        }
        Member member = optToken.get().getMember();
        return member;
    }


    public void logout(LogoutRequestDto logoutRequestDto) {
        //토큰 유효성 검증
        jwtTokenUtil.validateRefreshToken(logoutRequestDto.getRefreshToken());
        Optional<RefreshToken> optToken = refreshTokenRepository.findByToken(logoutRequestDto.getRefreshToken());
        if (optToken.isEmpty()) {
            throw new InvalidTokenException();
        }
        RefreshToken refreshToken = optToken.get();
        refreshTokenRepository.delete(refreshToken);
    }


    //이미 가입된 사용자인지 확인
    public Member getMemberBySocialId(String socialId) {
        return memberRepository.findBySocialId(socialId).orElse(null);
    }

    //Oauth 사용자 생성
    public Member createOauth(String socialId, String email, SocialType socialType, String nickname) {
        Member member;
        if (nickname == null || nickname.isBlank()) {
            member = Member.builder().userId(email).socialType(socialType).socialId(socialId)
                    .build();
        } else {
            member = Member.builder().userId(email).socialType(socialType).socialId(socialId)
                    .nickName(nickname + UUID.randomUUID())
                    .build();
        }
        memberRepository.save(member);
        return member;
    }


    //전화번호 및 UID 중복 검사
    public void verifyPhoneAuthInfo(PhoneAuthInfoDto phoneAuthInfo) {
        Optional<Member> findMember = memberRepository.findByFirebaseUidAndPhoneNumber(
                phoneAuthInfo.getUid(), phoneAuthInfo.getPhoneNumber());
        if (findMember.isPresent()) {
            throw new PhoneAlreadyException();
        }
    }

}
