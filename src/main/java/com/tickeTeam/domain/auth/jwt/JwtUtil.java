package com.tickeTeam.domain.auth.jwt;

import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.auth.service.CustomUserDetailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    private final SecretKey secretKey;
    private final CustomUserDetailService userDetailService;

    public JwtUtil(@Value("${jwt.secretKey}") String secret, CustomUserDetailService userDetailService) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                SIG.HS256.key().build().getAlgorithm());
        this.userDetailService = userDetailService;
    }

    // email 추출
    public String getEmail(String token){
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("memberEmail", String.class);
    }

    // identity 추출
    public Long getMemberIdentity(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("memberIdentity", Long.class);
    }

    // role 추출
    public String getUserRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("memberRole", String.class);
    }

    // UserDetails 조회 및 Authentication 객체 생성
    public Authentication getAuthentication(String email) {
        UserDetails userDetails = userDetailService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // 토큰 만료기간 검증
    public boolean isExpired(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    // 토큰 생성
    public String createJwt(String type, String email, MemberRole role, Long memberIdentity) {

        log.info("createJwt - type: {}, email: {}, role: {}, memberIdentity: {}", type, email, role,
                memberIdentity);

        long expirationTime = "access".equals(type)
                ? accessTokenExpirationPeriod
                : refreshTokenExpirationPeriod;

        return Jwts.builder()
                .claim("type", type)
                .claim("email", email)
                .claim("memberRole", role.name())
                .claim("memberIdentity", memberIdentity)
                .notBefore(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
}
