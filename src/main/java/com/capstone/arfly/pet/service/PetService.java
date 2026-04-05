package com.capstone.arfly.pet.service;

import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.BusinessException;
import com.capstone.arfly.common.exception.ErrorCode;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.common.util.S3Uploader;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.pet.domain.Breeds;
import com.capstone.arfly.pet.domain.Pet;
import com.capstone.arfly.pet.domain.PetAllergy;
import com.capstone.arfly.pet.domain.Species;
import com.capstone.arfly.pet.dto.CreatePetRequest;
import com.capstone.arfly.pet.repository.BreedsRepository;
import com.capstone.arfly.pet.repository.PetAllergyRepository;
import com.capstone.arfly.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetAllergyRepository petAllergyRepository;
    private final MemberRepository memberRepository;
    private final BreedsRepository breedsRepository;
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void createPet(Long memberId, CreatePetRequest request, MultipartFile petFile ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_EXISTS));

        Breeds breeds = breedsRepository.findByName(request.getBreeds())
                .orElseThrow(()-> new BusinessException(ErrorCode.BREED_NOT_FOUND));

        File profileImage = null;
        if(petFile != null && !petFile.isEmpty()){
            FileDetailDto metaData = s3Uploader.makeMetaData(petFile, "pets");
            s3Uploader.uploadFile(metaData.getKey(), petFile);

            profileImage = File.builder()
                    .fileName(metaData.getOriginalFileName())
                    .fileKey(metaData.getKey())
                    .fileSize(metaData.getFileSize())
                    .fileType(metaData.getFileType())
                    .deleted(false)
                    .build();
            profileImage = fileRepository.save(profileImage);
        }

        Pet pet = Pet.builder()
                .member(member)
                .breeds(breeds)
                .profileImage(profileImage)
                .name(request.getName())
                .birth(extractYear(request.getBirth()))
                .weight(request.getWeight())
                .neutered(request.isNeutered())
                .species(request.getSpecies())
                .sex(request.getSex())
                .note(request.getNote())
                .build();

        petRepository.save(pet);

        if(request.getPetAllergies() != null && !request.getPetAllergies().isEmpty()){
            for(String allergyName : request.getPetAllergies()){
                PetAllergy petAllergy = PetAllergy.builder()
                        .pet(pet)
                        .name(allergyName)
                        .build();
                petAllergyRepository.save(petAllergy);
            }
        }

    }

    private Integer extractYear(String birthStr) {
        // 아예 값이 안 들어왔을 때의 방어
        if (birthStr == null || birthStr.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_HEADER);
        }

        // 양옆 공백을 자름 (" 2016-12-22 " -> "2016-12-22")
        String trimmedBirth = birthStr.trim();

        // 길이가 4글자 안되면 애초에 잘못된 형식
        if (trimmedBirth.length() < 4) {
            throw new BusinessException(ErrorCode.INVALID_HEADER);
        }

        try {
            // 공백 제거 문자열에서 앞 4글자만 잘라 숫자로 변환
            return Integer.parseInt(trimmedBirth.substring(0, 4));
        } catch (NumberFormatException e) {
            //abcd-12-22처럼 숫자가 아닌 값을 보냈을 때의 방어
            throw new BusinessException(ErrorCode.INVALID_HEADER);
        }
    }

    @Transactional
    public List<String> getBreedsBySpecies(Species species) {
        List<Breeds> breedsList = breedsRepository.findBySpecies(species);

        return breedsList.stream()
                .map(Breeds::getName)
                .toList();
    }

}