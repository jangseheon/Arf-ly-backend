package com.capstone.arfly.diagnosis.repository;

import com.capstone.arfly.diagnosis.domain.DiagnosisImage;
import com.capstone.arfly.diagnosis.domain.DiagnosisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosisImageRepository extends JpaRepository<DiagnosisImage, Long> {

    // 파일 한번에 가져오기
    @Query("SELECT di FROM DiagnosisImage di JOIN FETCH di.file WHERE di.diagnosisReport IN :reports")
    List<DiagnosisImage> findAllByDiagnosisReportInWithFile(@Param("reports") List<DiagnosisReport> reports);
}