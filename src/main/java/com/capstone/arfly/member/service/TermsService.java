package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.EmptyTermsAgreementException;
import com.capstone.arfly.common.exception.InvalidTermsIdException;
import com.capstone.arfly.common.exception.UserNotExistsException;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.domain.Terms;
import com.capstone.arfly.member.domain.UserTermsAgreement;
import com.capstone.arfly.member.dto.LatestTermsResponseDto;
import com.capstone.arfly.member.dto.UserAgreementDto;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.member.repository.TermsRepository;
import com.capstone.arfly.member.repository.UserTermsAgreementRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsRepository termsRepository;
    private final MemberRepository memberRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    @Transactional(readOnly = true)
    public List<LatestTermsResponseDto> getLatestAgreements() {
        List<Terms> latestTemrsList = termsRepository.findByLatestTrue();

        List<LatestTermsResponseDto> response = latestTemrsList.stream()
                .map(LatestTermsResponseDto::from)
                .sorted(Comparator.comparing(LatestTermsResponseDto::getOrderIndex))
                .toList();
        return response;
    }

    @Transactional(readOnly = true)
    public boolean hasAgreedToLatestTerms(Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> {
            throw new UserNotExistsException();
        });

        List<UserTermsAgreement> userTermsAgreements = userTermsAgreementRepository.findByMemberId(memberId);
        if (userTermsAgreements == null || userTermsAgreements.isEmpty()) {
            return false;
        }
        return true;
    }

    @Transactional
    public void agreeToTerms(Long memberId, List<UserAgreementDto> userAgreements) {
        Member member = memberRepository.getReferenceById(memberId);
        //동의 항목 없음
        if (userAgreements == null || userAgreements.isEmpty()) {
            throw new EmptyTermsAgreementException();
        }

        // Id 리스트 추출
        List<Long> agreementList = userAgreements.stream()
                .map(UserAgreementDto::getTermId)
                .distinct()
                .toList();

        // DB에서 가져온 약관을 Map 형태로 변환 (Key: 약관 ID, Value: 약관 Entity)
        Map<Long, Terms> termsMap = termsRepository.findByIdIn(agreementList).stream()
                .collect(Collectors.toMap(Terms::getId, Function.identity()));

        // 클라이언트가 보낸 약관 ID가 DB에 전부 존재하는지 검증
        if (termsMap.size() != agreementList.size()) {
            throw new InvalidTermsIdException();
        }

        // 약관 동의 엔티티 생성
        List<UserTermsAgreement> userTermsAgreementList = userAgreements.stream()
                .map(dto -> UserTermsAgreement.builder()
                        .member(member)
                        .terms(termsMap.get(dto.getTermId()))
                        .agreement(dto.getTermsOfServiceAgreed())
                        .build())
                .toList();

        // 동의 약관 저장
        userTermsAgreementRepository.saveAll(userTermsAgreementList);
    }


}
