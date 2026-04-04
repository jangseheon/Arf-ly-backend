package com.capstone.arfly.common.repository;

import com.capstone.arfly.common.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findAllByDeletedTrue();

}
