package com.capstone.arfly.member.repository;

import com.capstone.arfly.member.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String refreshToken);
}
