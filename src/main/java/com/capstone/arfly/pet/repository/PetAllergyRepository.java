package com.capstone.arfly.pet.repository;

import com.capstone.arfly.pet.domain.Pet;
import com.capstone.arfly.pet.domain.PetAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetAllergyRepository extends JpaRepository<PetAllergy, Long> {

    void deleteAllByPet(Pet pet);

    List<PetAllergy> findAllByPet(Pet pet);

}
