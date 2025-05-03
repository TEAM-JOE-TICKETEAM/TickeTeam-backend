package com.tickeTeam.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickeTeam.domain.member.entity.TokenTypes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 접근 권한 허용 경로 검증
        if (isAllowedRequest(request)){
            filterChain.doFilter(request, response);
            return;
        }

        // Header에서 Access-Token 추출
        String tokenHeader = request.getHeader(TokenTypes.ACCESS.getType());

        // Bearer 시작 여부 및 null 값 검증
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            log.info("request URI : {}", request.getRequestURI());
            log.info("Invalid or missing access-token");
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = tokenHeader.substring(7);

        // Access-Token 만료시간 검증
        if (jwtUtil.isExpired(accessToken)) {
            log.info("access-token is expired");
            setResponse(response);
            return;
        }

        setAuthentication(accessToken);
        filterChain.doFilter(request, response);
    }

    // email을 추출 및 인증 정보를 설정
    private void setAuthentication(String token) {
        String email = jwtUtil.getEmail(token);

        Authentication authentication = jwtUtil.getAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Access-Token 만료시 401 반환
    private void setResponse(HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> body = Map.of("message", "access-token 만료");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    // 접근 권한 허용 경로 검증 (true -> 통과, false -> 예외)
    private boolean isAllowedRequest(HttpServletRequest request) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
            String path = request.getRequestURI();

        // 1. Options 요청 검증
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 2. 예외 경로 검증
        return isExcludedPath(pathMatcher, path);
    }

    // Jwt 검증 제외 경로
    private boolean isExcludedPath(AntPathMatcher pathMatcher, String requestURI) {

        return pathMatcher.match("/api/v1/member/login/**", requestURI)
                || pathMatcher.match("/api/v1/member/signup", requestURI)
                || pathMatcher.match("/api/v1/member/signup/**", requestURI)
                || pathMatcher.match("/v3/api-docs/**", requestURI)
                || pathMatcher.match("/swagger-ui/**", requestURI)
                || pathMatcher.match("/swagger-ui.html", requestURI)
                || pathMatcher.match("/swagger-resources/**", requestURI)
                || pathMatcher.match("/actuator/**", requestURI);
    }
}
