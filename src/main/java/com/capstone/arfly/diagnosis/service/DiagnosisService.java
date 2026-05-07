package com.capstone.arfly.diagnosis.service;

import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.BusinessException;
import com.capstone.arfly.common.exception.ErrorCode;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.common.util.S3Uploader;
import com.capstone.arfly.diagnosis.domain.DiagnosisImage;
import com.capstone.arfly.diagnosis.domain.DiagnosisReport;
import com.capstone.arfly.diagnosis.dto.AiResponse;
import com.capstone.arfly.diagnosis.dto.DiagnosisListResponseDto;
import com.capstone.arfly.diagnosis.dto.DiagnosisResponseDto;
import com.capstone.arfly.diagnosis.repository.DiagnosisImageRepository;
import com.capstone.arfly.diagnosis.repository.DiagnosisReportRepository;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.pet.domain.Pet;
import com.capstone.arfly.pet.domain.PetAllergy;
import com.capstone.arfly.pet.repository.PetAllergyRepository;
import com.capstone.arfly.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final PetRepository petRepository;
    private final PetAllergyRepository petAllergyRepository;
    private final DiagnosisReportRepository diagnosisReportRepository;
    private final DiagnosisImageRepository diagnosisImageRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;
    private final WebClient webClient;
    private final ChatClient chatClient;


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

    @Transactional
    public DiagnosisResponseDto getDiagnosis(Long petId, MultipartFile file, Long userId){
        Pet pet = petRepository.findById(petId).orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));

        if(!pet.getMember().getId().equals(userId)) throw new BusinessException(ErrorCode.PET_OWNER_MISMATCH);

        String spices = pet.getSpecies().name();

        AiResponse response;

        try {
            response = diagnosisSkin(file, spices);
        } catch (Exception e){
            throw new BusinessException(ErrorCode.AI_MODEL_ERROR);
        }

        String disease = cleanDiseaseName(response.getPrediction().getDisease());
        String probabilityText = response.getPrediction().getProbability();
        Double probability =  Double.parseDouble(probabilityText.replace("%", ""));

        String management = "";

        try {
            management = createManagement(pet,disease,probabilityText);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPENAI_API_ERROR);
        }

        FileDetailDto metaData = s3Uploader.makeMetaData(file, "pets");
        s3Uploader.uploadFile(metaData.getKey(), file);

        File image = File.builder()
                .fileName(metaData.getOriginalFileName())
                .fileKey(metaData.getKey())
                .fileSize(metaData.getFileSize())
                .fileType(metaData.getFileType())
                .deleted(false)
                .build();

        image = fileRepository.save(image);

        String imageUrl = s3Uploader.getPublicUrl(image.getFileKey());

        DiagnosisReport report = DiagnosisReport.builder()
                .pet(pet)
                .diseaseName(disease)
                .probability(probability)
                .management(management)
                .build();

        DiagnosisReport savedReport = diagnosisReportRepository.save(report);

        DiagnosisImage diagnosisImage = DiagnosisImage.builder()
                .diagnosisReport(savedReport)
                .file(image)
                .build();

        diagnosisImageRepository.save(diagnosisImage);

        return DiagnosisResponseDto.builder()
                .id(savedReport.getId())
                .petName(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreeds().getName())
                .sex(pet.getSex())
                .neutered(pet.getNeutered())
                .birth(pet.getBirth())
                .imageUrl(imageUrl)
                .diseaseName(savedReport.getDiseaseName())
                .probability(savedReport.getProbability())
                .management(savedReport.getManagement())
                .build();
    }

    public DiagnosisResponseDto getDiagnosisDetail(Long reportId, Long userId){

        memberRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        DiagnosisReport report = diagnosisReportRepository.findById(reportId).orElseThrow(
                () -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        Pet pet = report.getPet();

        petRepository.findById(pet.getId()).orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));

        DiagnosisImage reportImage = diagnosisImageRepository.findByDiagnosisReport(report);
        String fileKey = reportImage.getFile().getFileKey();
        String imageUrl = s3Uploader.getPublicUrl(fileKey);

        return DiagnosisResponseDto.builder()
                .id(report.getId())
                .petName(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreeds().getName())
                .sex(pet.getSex())
                .neutered(pet.getNeutered())
                .birth(pet.getBirth())
                .imageUrl(imageUrl)
                .diseaseName(report.getDiseaseName())
                .probability(report.getProbability())
                .management(report.getManagement())
                .build();
    }

    private AiResponse diagnosisSkin(MultipartFile file, String spices) throws IOException {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // ai 서버에 보낼 file 생성
        builder.part("file", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                })
                .contentType(MediaType.parseMediaType(file.getContentType()));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/diagnose")
                        .queryParam("animal", spices)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(AiResponse.class)
                .block();
    }

    private String cleanDiseaseName(String diseaseName) {
        if (diseaseName == null) {
            return null;
        }

        if (diseaseName.startsWith("정상")) {
            return "정상";
        }

        if (diseaseName.startsWith("A") && diseaseName.length() > 3) {
            return diseaseName.substring(3);
        }

        return diseaseName;
    }

    private String createManagement(Pet pet, String disease, String probabilityText){

        List<PetAllergy> allergies = petAllergyRepository.findAllByPet(pet);

        String prompt = """
                당신은 반려동물 피부 건강 관리 안내문을 작성하는 도우미입니다.
                
                [반려동물 정보]
                이름: %s
                태어난 년도: %s
                품종: %s
                알레르기 정보: %s
                특이사항: %s
                
                [AI 피부 분석 결과]
                의심 질환: %s
                예측 확률: %s
                
                """.formatted(
                pet.getName(),
                pet.getBirth(),
                pet.getBreeds(),
                allergies.isEmpty() ? "없음" : allergies,
                pet.getNote() == null ? "없음" : pet.getNote(),
                disease,
                probabilityText
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
