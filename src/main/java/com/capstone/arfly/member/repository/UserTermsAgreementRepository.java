package com.capstone.arfly.member.repository;

import com.capstone.arfly.member.domain.UserTermsAgreement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {

    @Query("""
                SELECT u FROM UserTermsAgreement u JOIN u.terms t WHERE u.member.id = :memberId  AND t.latest = true AND t.required = true
            """)
    List<UserTermsAgreement> findByMemberId(@Param("memberId") Long memberId);
}
