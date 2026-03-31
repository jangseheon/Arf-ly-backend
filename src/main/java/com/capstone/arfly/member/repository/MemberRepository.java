package com.capstone.arfly.member.repository;

import com.capstone.arfly.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByUserId(String userId);
    Optional<Member> findByNickName(String nickname);
    Optional<Member> findByFirebaseUidAndPhoneNumber(String uid, String phoneNumber);
    Optional <Member> findBySocialId(String socialId);
}
