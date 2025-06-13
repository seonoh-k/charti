package com.example.demo.filter;

import com.example.demo.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 토큰 까기
 *
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 우리가 생성한 Jwt Token Name
    private static final String TOKEN_NAME = "token";
    // 토큰이 필요 없는 URI 목록
    private static final String[] PUBLIC_PATHS = {
            "/loginForm",
            "/joinForm",
            "/oauth2/login",
            "/join",
            "/login",
            "/expertJoinForm",
            "/managerJoinForm",
            "/api/check-phone",
            "/favicon.ico"
    };

    // 정적 리소스 및 .well-known 경로를 허용할 때 사용하는 접두사
    private static final String[] PREFIX_WHITELIST = {
            "/css/",
            "/js/",
            "/images/",
            "/.well-known/"
    };

    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                log.info("🍪[Filter] JwtAuthenticationFilter.resolveToken : COOKIE Name : {} ",c.getName());
                Map<String, String> attributes = c.getAttributes();
                for(String key : attributes.keySet()){
                    log.info("🍪[Filter] JwtAuthenticationFilter.resolveToken : 🔑 key : {}  📦 value : {} ",key,attributes.get(key));
                }

                if (TOKEN_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("🔒 [Filter] JwtAuthenticationFilter.doFilterInternal :  현재 검증 중인 경로 = {}", path);

        // login, loginForm 등 인증이 필요 없는 웹사이트는 토큰을 검증 하지 않는다.
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        for (String prefix : PREFIX_WHITELIST) {
            if (path.startsWith(prefix)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // JWT 존재 여부 확인
        String token = resolveToken(request);
        if (token == null) {
            throw new AuthenticationCredentialsNotFoundException("JWT 토큰이 없습니다.");
        }

        try {

            Authentication auth = jwtUtil.getAuthentication(token);

            // 토큰에서 가져온 권한을 기반으로 ContextHolder에 저장한다.
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
            throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다.");
        }
        filterChain.doFilter(request, response);
    }
}
