package com.capstone.arfly.pet.repository;

import com.capstone.arfly.pet.domain.Pet;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("""
    SELECT p FROM Pet  p LEFT JOIN FETCH p.profileImage WHERE p.member.id = :memberId 
    """)
    List<Pet> findAllByMemberId(@Param("memberId") Long memberId);

    @EntityGraph(attributePaths = {"member","breeds","profileImage"})
    Optional<Pet>findById(long petId);

}
