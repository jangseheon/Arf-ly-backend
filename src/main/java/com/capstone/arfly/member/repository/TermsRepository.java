package com.capstone.arfly.member.repository;

import com.capstone.arfly.member.domain.Terms;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    List<Terms> findByIdIn(List<Long> idList);
    List<Terms> findByLatestTrue();
}
