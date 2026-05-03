package com.capstone.arfly.diagnosis.service;

import com.capstone.arfly.common.util.S3Uploader;
import com.capstone.arfly.diagnosis.domain.DiagnosisImage;
import com.capstone.arfly.diagnosis.domain.DiagnosisReport;
import com.capstone.arfly.diagnosis.dto.DiagnosisListResponseDto;
import com.capstone.arfly.diagnosis.repository.DiagnosisImageRepository;
import com.capstone.arfly.diagnosis.repository.DiagnosisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisReportRepository diagnosisReportRepository;
    private final DiagnosisImageRepository diagnosisImageRepository;
    private final S3Uploader s3Uploader;


    @Transactional(readOnly = true)
    public DiagnosisListResponseDto getDiagnosisList(Long memberId, Long petId,Long cursor, int size) {

        PageRequest pageRequest = PageRequest.of(0, size+1);
        List<DiagnosisReport> reports = diagnosisReportRepository.findReportsWithPaging(memberId, petId, cursor, pageRequest);

        boolean hasNext = reports.size() > size;
        if(hasNext){
            reports.remove(size);
        }

        Long nextCursor = reports.isEmpty() ? null : reports.get(reports.size()-1).getId();

        return createDiagnosisListResponse(reports, hasNext, nextCursor, size);
    }

    public DiagnosisListResponseDto createDiagnosisListResponse(List<DiagnosisReport> reports, boolean hasNext, Long nextCursor, int size){
        // 조회된 리포트들의 이미지를 한번의 쿼리로 다 가져오기
        List<DiagnosisImage> allImages = diagnosisImageRepository.findAllByDiagnosisReportInWithFile(reports);

        // 가져온 이미지들을 리포트 id 기준으로 map한다.
        Map<Long, List<DiagnosisImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getDiagnosisReport().getId()));

        List<DiagnosisListResponseDto.DiagnosisSummary> summaries = reports.stream().map(report -> {

            String imageUrl = null;
            List<DiagnosisImage> reportImages = imageMap.getOrDefault(report.getId(), Collections.emptyList());

            if (!reportImages.isEmpty()) {
                String fileKey = reportImages.get(0).getFile().getFileKey();
                imageUrl = s3Uploader.getPublicUrl(fileKey);
            }

            return DiagnosisListResponseDto.DiagnosisSummary.builder()
                    .id(report.getId())
                    .createdAt(report.getCreatedAt().toLocalDate().toString())
                    .imageUrl(imageUrl)
                    .diseaseName(report.getDiseaseName())
                    .petName(report.getPet().getName())
                    .breedName(report.getPet().getBreeds().getName())
                    .birthYear(report.getPet().getBirth())
                    .build();

        }).toList();

        return DiagnosisListResponseDto.builder()
                .diagnoses(summaries)
                .meta(DiagnosisListResponseDto.Meta.builder()
                        .hasNext(hasNext)
                        .nextCursor(nextCursor)
                        .size(size)
                        .build())
                .build();
    }
}
