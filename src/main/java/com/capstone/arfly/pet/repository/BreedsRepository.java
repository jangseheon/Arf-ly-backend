package com.capstone.arfly.pet.repository;

import com.capstone.arfly.pet.domain.Breeds;
import com.capstone.arfly.pet.domain.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreedsRepository extends JpaRepository<Breeds, Long> {

    Optional<Breeds> findByName(String name);

    List<Breeds> findBySpecies(Species species);
}
