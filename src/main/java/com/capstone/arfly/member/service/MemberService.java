package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.BusinessException;
import com.capstone.arfly.common.exception.ErrorCode;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.domain.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.capstone.arfly.member.dto.UpdateUserRequest;
import com.capstone.arfly.member.dto.UserNameCheckRequestDto;
import com.capstone.arfly.member.dto.UserProfileResponse;
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
    private final PasswordEncoder passwordEncoder;

    public boolean isUsernameAvailable(UserNameCheckRequestDto userNameCheckRequestDto) {
        Optional<Member> member = memberRepository.findByNickName(userNameCheckRequestDto.getNickname());
        if(member.isPresent()){
            return false;
        }
        return true;
    }

    // 내 프로필 정보 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_EXISTS));

        boolean isDoctor = Role.DOCTOR.equals(member.getRole());

        return UserProfileResponse.builder()
                .userId(member.getId())
                .nickname(member.getNickName())
                .doctor(isDoctor)
                .notificationEnabled(member.isNotificationEnabled())
                .phoneNumber(member.getPhoneNumber())
                .latitude(member.getLatitude())
                .longitude(member.getLongitude())
                .roadAddress(member.getRoad_address())
                .build();

    }

    // 내 정보 수정
    @Transactional
    public void updateMyProfile(Long memberId, UpdateUserRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        member.updateProfile(
                request.getNickname(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRoadAddress(),
                request.isNotificationEnabled()
        );

        String rawPassword = request.getPassword();
        if (rawPassword != null && !rawPassword.isBlank()) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            member.updatePassword(encodedPassword);
        }
    }


}
