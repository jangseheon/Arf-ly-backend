package com.capstone.arfly.diagnosis.repository;

import com.capstone.arfly.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capstone.arfly.diagnosis.domain.DiagnosisReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiagnosisReportRepository extends JpaRepository<DiagnosisReport, Long> {

    @Query("SELECT r FROM DiagnosisReport r " +
            "JOIN FETCH r.pet p " +
            "JOIN FETCH p.breeds b " +
            "WHERE p.member.id = :memberId " +
            "AND (:petId IS NULL OR p.id = :petId) " +
            "AND (:cursor IS NULL OR r.id < :cursor) " +
            "ORDER BY r.id DESC")
    List<DiagnosisReport> findReportsWithPaging(
            @Param("memberId") Long memberId,
            @Param("petId") Long petId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    Optional<DiagnosisReport> findByPet(Pet pet);
}
