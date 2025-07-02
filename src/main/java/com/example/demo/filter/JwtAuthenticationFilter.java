package com.example.demo.filter;

import com.example.demo.dto.auth.AdminAuthDTO;
import com.example.demo.dto.auth.UserAuthDTO;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.users.repository.AdminQueryRepository;
import com.example.demo.users.repository.UserQueryRepository;
import com.example.demo.util.AppURLs;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 토큰 까기
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserQueryRepository userQueryRepository;
    private final AdminQueryRepository adminQueryRepository;

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

            // 🔹 사용자 UID 추출 (토큰 구조에 따라 email도 가능)
            String uuid = jwtUtil.getUuid(token);
            log.info("========================❌ ❌ ❌ ❌ ❌ ❌ 토큰에서 추출한 username  {}: ",uuid);
            // 🔹 DB에서 사용자 조회
            Optional<UserAuthDTO> userAuthDTOByUuid = userQueryRepository.getUserAuthDTOByUuid(uuid);
            Optional<AdminAuthDTO> adminAuthDTOByUuid = adminQueryRepository.getAdminAuthDTOByUuid(uuid);
            if (userAuthDTOByUuid.isEmpty() && adminAuthDTOByUuid.isEmpty()){
                throw new UsernameNotFoundException("탈퇴되었거나 존재하지 않는 사용자");
            }

            if (userAuthDTOByUuid.isPresent()){
                UserAuthDTO userAuthDTO = userAuthDTOByUuid.get();
                // 🔒 삭제된 사용자 차단
                if (userAuthDTO.isDeleted()) {
                    log.warn("❌ 탈퇴한 사용자 접근 차단: {}", uuid);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "탈퇴한 사용자입니다.");
                    return;
                }
            }

            if (adminAuthDTOByUuid.isPresent()){
                AdminAuthDTO adminAuthDTO = adminAuthDTOByUuid.get();
                // 🔒 삭제된 사용자 차단
                if (adminAuthDTO.isDeleted()) {
                    log.warn("❌ 탈퇴한 사용자 접근 차단: {}", uuid);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "탈퇴한 사용자입니다.");
                    return;
                }
            }

            // 토큰에서 가져온 권한을 기반으로 ContextHolder에 저장한다.
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
            throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다.");
        }
        filterChain.doFilter(request, response);
    }

}
