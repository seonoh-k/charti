package com.example.demo.jwt;

import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Log4j2
public class JwtUtil {

    // 생성자 방식으로 @Value 값을 주입하면,
    // 컴파일 타임에서 초기화 보장이 되므로 final 사용이 가능
    private final String COOKIE_NAME = "token";
    private final String SECRET_KEY;
    private final Long EXPIRATION_TIME;

    private final Key key;

    public JwtUtil(@Value("${jwt.secret-key}") String SECRET_KEY,
                   @Value("${jwt.expiration}") Long EXPIRATION_TIME) {

        this.SECRET_KEY = SECRET_KEY;
        this.EXPIRATION_TIME = EXPIRATION_TIME;
        //this.key = Keys.secretKeyFor(signatureAlgorithm);
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        //

    }



    /**
     * 파이어베이스 액세스 토큰을 기반으로 JWT 토큰을 발급한다.
     *
     * @param firebaseToken : 파이어베이스 토큰을 의미한다.
     * @return 어플리케이션에서 지속적으로 사용하는 JWT 토큰을 발급한다.
     */
    public String createToken(FirebaseToken firebaseToken) {
        String role = (String)firebaseToken.getClaims().get("role");
        log.info("🍪 [POST] AuthController.login -> JwtUtil.createToken(FirebaseToken firebaseToken) role =>  {}",role);
        return Jwts.builder()
                .setSubject(firebaseToken.getEmail())
                .setClaims(firebaseToken.getClaims())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출하여 이메일을 반환한다.
     *
     * @param token : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return : username == email 이메일을 반환한다.
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    /**
     * JWT 토큰에서 사용자 정보를 추출하여 권한정보를 반환한다.
     *
     * @param token : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return String ex)  ROLE_ADMIN,ROLE_USER,ROLE_EXPERT,ROLE_MANAGER
     */
    public String getRole(String token) {
        String role = (String) Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
        log.info("getRole => {}",role);
        return role;
    }

    /**
     *
     *
     * @param token : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return : true : 검사결과에 부합
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Json
     *
     * @param token : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return : JWT 토큰에 있는 payload의 claims를 반환한다.
     */
    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)        // JWT 서명에 사용한 키 설정
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT를 쿠키로 변환한다.
     * **HttpOnly, Secure, SameSite은 보안 설정이며 메서드 구현부의 주석 참조**
     *
     * @param jwt : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return : 유효시간이 1시간인 JWT 토큰 값이 들어있는 쿠키를 반환한다.
     */
    public ResponseCookie createCookie(String jwt){
        return ResponseCookie.from(COOKIE_NAME, jwt) // 쿠키 이름과 값 설정
                .httpOnly(true)                // JavaScript 접근 차단 (XSS 방지용)
                .secure(true)                  // HTTPS에서만 전송 (개발 중에는 false 가능)
                .path("/")                     // 쿠키 유효 경로
                .maxAge(60 * 60)               // 쿠키 유효 시간 (초 단위, 여기선 1시간)
                .sameSite("Strict")            // CSRF 보호 강화 (또는 "Lax" 사용 가능)
                .build();
    }

    /**
     * JWT에서 사용자 정보 추출하고 유저의 식별자 정보, Role 정보를 기반으로 UsernamePasswordAuthenticationToken를 생성한다.
     * @param token : 어플리케이션 서버에서 발급한 JWT 토큰을 의미한다.
     * @return : UsernamePasswordAuthenticationToken 객체를 반환한다.
     */
    public Authentication getAuthentication(String token) {
        String username = getUsername(token); // JWT에서 username 추출
        String role = getRole(token);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add((SimpleGrantedAuthority) new SimpleGrantedAuthority(role));
        return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
    }
}
