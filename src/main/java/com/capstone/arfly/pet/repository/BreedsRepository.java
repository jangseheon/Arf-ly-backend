package com.capstone.arfly.pet.repository;

import com.capstone.arfly.pet.domain.Breeds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BreedsRepository extends JpaRepository<Breeds, Long> {

    Optional<Breeds> findByName(String name);
}
