package com.example.demo.oauth;

import com.example.demo.users.entity.Role;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final List<OAuth2UserInfoFactory> userInfoFactories;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("🔑 OAuth2 로그인 요청 성공: {}", userRequest.getClientRegistration().getRegistrationId());

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
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority(Role.ROLE_MEMBER.name()));

            return new DefaultOAuth2User(grantedAuthorities,oAuth2UserInfo.getAttributes(),oAuth2UserInfo.getProviderIdKey());


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

