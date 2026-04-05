package com.capstone.arfly.pet.init;

import com.capstone.arfly.pet.domain.Breeds;
import com.capstone.arfly.pet.domain.Species;
import com.capstone.arfly.pet.repository.BreedsRepository;
import com.capstone.arfly.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class BreedDataInit implements CommandLineRunner {
    private final BreedsRepository breedsRepository;

    @Override
    public void run(String... args) throws Exception {
        // 중복 저장 방지
        if (breedsRepository.count() > 0) {
            log.info("품종 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        log.info("CSV 파일에서 품종 데이터를 읽어 DB에 저장합니다...");
        loadBreeds("data/dog_breeds.csv", Species.DOG);
        loadBreeds("data/cat_breeds.csv", Species.CAT);

        log.info("모든 품종 데이터 초기화가 완벽하게 끝났습니다! 🚀");
    }

    //   CSV 읽기 및 저장
    private void loadBreeds(String path, Species species) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            int count = 0; // 몇 개 저장되는지 확인하기 위한 카운트

            while ((line = br.readLine()) != null) {
                String breedName = line.trim();

                if (!breedName.isEmpty()) {
                    // 엔티티 생성 후 DB에 저장
                    breedsRepository.save(Breeds.builder()
                            .name(breedName)
                            .species(species)
                            .build());
                    count++;
                }
            }
            log.info("[{}] 품종 {}개 세팅 완료!", species, count);

        } catch (Exception e) {
            log.error("{} 파일을 읽거나 저장하는 중 오류가 발생했습니다: {}", path, e.getMessage());
        }
    }


}
