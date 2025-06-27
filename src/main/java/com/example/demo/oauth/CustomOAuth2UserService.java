package com.example.demo.oauth;

import com.example.demo.dto.request.LoginAttemptRequest;
import com.example.demo.users.entity.Role;
import com.example.demo.users.service.AuthService;
import com.example.demo.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.example.demo.users.repository.UserRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final List<OAuth2UserInfoFactory> userInfoFactories;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final IpUtils ipUtils;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("🔑 OAuth2 로그인 요청 성공: {}", userRequest.getClientRegistration().getRegistrationId());
        String email = null;
        try {
            // "google", "naver", "kakao"
            String provider = userRequest.getClientRegistration().getRegistrationId();
            log.info("👤 [CustomOAuth2UserService.loadUser] provider : {}",provider);

            // provider를 하나씩 가져와서 검사 후 해당하는 factory를 생성한다.
            OAuth2UserInfoFactory factory = userInfoFactories.stream()
                    .filter(f -> f.supports(provider))
                    .findFirst()
                    .orElseThrow(() ->
                            new OAuth2AuthenticationException("Unsupported provider: " + provider)
                    );

            OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();
            OAuth2UserInfo oAuth2UserInfo = factory.create(attributes);

            log.info("🍘 CustomOAuth2UserService.loadUser oAuth2UserInfo.getAttributes() : {}",oAuth2UserInfo.getAttributes());
            log.info("🍘 CustomOAuth2UserService.loadUser oAuth2UserInfo.getName() : {}",oAuth2UserInfo.getName());
            log.info("🍘 CustomOAuth2UserService.loadUser oAuth2UserInfo.getEmail() : {}",oAuth2UserInfo.getEmail());
            log.info("🍘 CustomOAuth2UserService.loadUser oAuth2UserInfo.getProviderId() : {}",oAuth2UserInfo.getProviderId());
            log.info("🍘 CustomOAuth2UserService.loadUser oAuth2UserInfo.getProvider() : {}",oAuth2UserInfo.getProvider());
            email = oAuth2UserInfo.getEmail();
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority(Role.ROLE_MEMBER.name()));

            return new DefaultOAuth2User(grantedAuthorities,oAuth2UserInfo.getAttributes(),oAuth2UserInfo.getProviderIdKey());


        } catch (Exception e) {
            // 1) HttpServletRequest 얻기
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs.getRequest();
            // 2) IP 추출
            String clientIp = IpUtils.extractClientIp(req);
            LoginAttemptRequest attemptRequest = new LoginAttemptRequest(email, false, "소셜 로그인 실패");
            authService.createUserLoginFailHistory(attemptRequest,clientIp);

            e.printStackTrace();
            throw e;
        }

    }

}

