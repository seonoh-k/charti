package com.example.demo.filter;

import com.example.demo.jwt.JwtUtil;
import com.example.demo.util.AppURLs;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 토큰 까기
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 우리가 생성한 Jwt Token Name
    private static final String TOKEN_NAME = "token";

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("🔒 [Filter] JwtAuthenticationFilter.doFilterInternal :  현재 검증 중인 경로 = {}", path);

        // login, loginForm 등 인증이 필요 없는 웹사이트는 토큰을 검증 하지 않는다.
        AntPathMatcher matcher = new AntPathMatcher();

        for (String publicPath : AppURLs.PUBLIC_URLS) {
            if (matcher.match(publicPath, path)) {
                log.info("🎫 PUBLIC 통과 : {}", path);
                filterChain.doFilter(request, response);
                return;
            }
        }

        for (String prefix : AppURLs.PREFIX_WHITELIST) {
            if (matcher.match(prefix, path)) {
                log.info("🎫 PREFIX 통과 : {}", path);
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
