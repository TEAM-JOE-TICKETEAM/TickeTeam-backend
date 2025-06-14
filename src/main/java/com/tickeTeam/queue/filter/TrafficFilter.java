package com.tickeTeam.queue.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import com.tickeTeam.queue.service.TrafficService;
import com.tickeTeam.queue.service.WaitingQueueService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class TrafficFilter implements Filter {

    private final TrafficService trafficService;
    private final WaitingQueueService waitingQueueService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // 1. 모든 요청에 대해 트래픽 카운트 증가
        trafficService.incrementRequestCount();

        // 2. 대기열 로직에서 제외할 URL
        String url = httpRequest.getRequestURI();
        if (url.startsWith("/api/v1/sse/") || url.startsWith("/api/v1/queue/rank")) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // 3. 대기열 시스템이 비활성화 상태이면, 모든 요청 즉시 통과
        if(!trafficService.isQueueActive()) {
            filterChain.doFilter(httpRequest,httpResponse);
            return;
        }

        log.info("대기열 시스템 활성화됨. 요청 URL: {}", url);

        // 4. 요청에서 사용자 ID 추출
        String tokenHeader = httpRequest.getHeader("Authorization");
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            log.info("request URI : {}", httpRequest.getRequestURI());
            log.info("Invalid or missing access-token");
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        String accessToken = tokenHeader.substring(7);
        String memberId = null;

        if (!jwtUtil.isExpired(accessToken)){
            memberId = jwtUtil.getMemberIdentity(accessToken).toString();
            log.info("추출된 사용자 ID = {}", memberId);
        }

        // 5. 사용자가 '허용 명단'에 있는지 확인
        // .block()을 사용하여 리액티브 스트림의 결과를 동기적으로 기다림
        boolean isAllowed = waitingQueueService.isAllowed(memberId).block();
        if (isAllowed) {
            // 허용된 사용자 -> 요청 통과 / 허용 명단에서 제거
            log.info("사용자 {} 진입 허용, 허용 명단에서 제거합니다.", memberId);
            waitingQueueService.markAsEntered(memberId).subscribe(); //비동기로 제거 요청
            filterChain.doFilter(httpRequest, httpResponse);
        } else {
            // 대기해야 하는 사용자 -> 대기열에 추가하고 대기 응답 반환
            log.info("사용자 {}를 대기열에 추가합니다.", memberId);
            Long rank = waitingQueueService.addQueue(memberId).block();

            httpResponse.setStatus(HttpStatus.ACCEPTED.value()); // 202 Accepted
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.setCharacterEncoding("UTF-8");

            // 클라이언트에게 대기 순번 정보 반환
            Map<String, Object> responseBody = Map.of(
                    "message", "서비스 접속 대기 중입니다.",
                    "rank", rank,
                    "memberId", memberId
            );

            httpResponse.getWriter().write(objectMapper.writeValueAsString(responseBody));
        }
    }
}
