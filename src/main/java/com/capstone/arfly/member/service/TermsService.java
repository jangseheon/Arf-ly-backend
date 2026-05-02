package com.capstone.arfly.member.service;

import com.capstone.arfly.member.domain.Terms;
import com.capstone.arfly.member.dto.LatestTermsResponseDto;
import com.capstone.arfly.member.repository.TermsRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsRepository termsRepository;

    @Transactional(readOnly = true)
    public List<LatestTermsResponseDto> getLatestAgreements() {
        List<Terms> latestTemrsList = termsRepository.findByLatestTrue();

        List<LatestTermsResponseDto> response = latestTemrsList.stream()
                .map(LatestTermsResponseDto::from)
                .sorted(Comparator.comparing(LatestTermsResponseDto::getOrderIndex))
                .toList();
        return  response;
    }
}
