package com.example.demo.oauth;


import com.example.demo.dto.UserDTO;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.users.entity.Role;
import com.example.demo.users.service.FirebaseService;
import com.example.demo.users.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final FirebaseService firebaseService;
    private final JwtUtil jwtUtil;
    private final List<OAuth2UserInfoFactory> userInfoFactories;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException, OAuth2AuthenticationException {

        log.info("1️⃣ OAuth2LoginSuccessHandler.onAuthenticationSuccess Start ");
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        // ex : kakao, naver, google
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        OAuth2UserInfoFactory userInfoFactory = userInfoFactories.stream()
                .filter(f -> f.supports(provider))
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException("Unsupported provider: " + provider));

        OAuth2UserInfo oAuth2UserInfo = userInfoFactory.create(attributes);

        UserDTO userDTO;
        // Firebase 사용자 생성 또는 확인
        // 추가 고려 사항 파이어베이스만 계정 정보가 등록되고 데이터베이스만 등록된경우
        // 파이어베이스 이상으로 파이어베이스는 저장되지않고 데이터베이스만 저장된 경우
        //
        try {
            boolean existsByEmailInFirebase = firebaseService.existsByByEmail(oAuth2UserInfo.getEmail());
            boolean existsByEmailInDB = userService.existsByEmail(oAuth2UserInfo.getEmail());
            // 존재하면 생성 X
            log.info("파이어베이스에 이메일이 중복인가? {}", existsByEmailInFirebase);
            log.info("데이터베이스에 이메일이 중복인가? {}", existsByEmailInDB);
            // member doesn't Exist anywhere firebase ,database
            if(!existsByEmailInFirebase && !existsByEmailInDB){

                userDTO = UserDTO.builder()
                        .providerId(oAuth2UserInfo.getProviderId())
                        .provider(oAuth2UserInfo.getProvider())
                        .name(oAuth2UserInfo.getName())
                        .username(oAuth2UserInfo.getEmail())
                        .role(Role.ROLE_MEMBER.name())
                        .build();

                userDTO = firebaseService.createSocialMember(userDTO);
                log.info("FB : createSocialMember ");
                userService.createSocialMember(userDTO);
                log.info("DB : createSocialMember ");
                firebaseService.setFirebaseMemberRoleToMember(userDTO);
                log.info("5️⃣ OAuth2LoginSuccessHandler.onAuthenticationSuccess Start 55  UUID : {} ", userDTO.getUuid());

                String customToken = firebaseService.createFirebaseCustomToken(userDTO.getUuid(), Map.of("role", userDTO.getRole()));
                RequestDispatcher dispatcher = request.getRequestDispatcher("/oauth2/login");
                HttpCookie cookie = jwtUtil.createCookie(customToken);
                request.setAttribute("firebaseCustomCookie", cookie.getValue());
                dispatcher.forward(request,response);
                return;

            } else if (existsByEmailInFirebase && existsByEmailInDB) {

                userDTO =  userService.getMemberByEmail(oAuth2UserInfo.getEmail());
                // 변경
                String customToken = firebaseService.createFirebaseCustomToken(userDTO.getUuid(), Map.of("role", userDTO.getRole()));
                log.info("customToken : {} ", customToken);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/oauth2/login");
                HttpCookie cookie = jwtUtil.createCookie(customToken);
                request.setAttribute("firebaseCustomCookie", cookie.getValue());
                dispatcher.forward(request, response);
                return;
            } else if(existsByEmailInFirebase && !existsByEmailInDB){
                // 이메일이 파이어 베이스에 있지만 데이터베이스에 없는 경우
                throw new RuntimeException("파이어베이스에 해당 계정을 삭제하고 다시시도 하세요 ");
            }
            else{
                throw new RuntimeException("데이터베이스에 해당 계정을 삭제하고 다시시도 하세요 ");
            }

        } catch (FirebaseAuthException e) {
            log.info("👊 OAuth2LoginSuccessHandler.onAuthenticationSuccess FirebaseAuthException Occured ");
            log.error("Firebase 사용자 처리 중 오류", e);
        }

    }
}
