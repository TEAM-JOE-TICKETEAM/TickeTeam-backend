package com.tickeTeam.domain.member.repository;

import com.tickeTeam.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m JOIN FETCH m.favoriteTeam WHERE m.email = :email")
    Optional<Member> findByEmailWithTeam(@Param("email") String email);
}
