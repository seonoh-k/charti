package com.example.demo.config;

import com.example.demo.filter.JwtAuthenticationFilter;
import com.example.demo.oauth.CustomOAuth2UserService;
import com.example.demo.oauth.OAuth2LoginSuccessHandler;
import com.example.demo.util.AppURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize

                        // 인증 필요 없는 URL (loginForm, join, 정적 자원 등)
                        .requestMatchers(AppURLs.getCombineURL(AppURLs.PUBLIC_URLS, AppURLs.PREFIX_WHITELIST)).permitAll()
                        .requestMatchers(AppURLs.getCombineURL(AppURLs.MEMBER_URLS, AppURLs.MEMBER_URLS_PREFIX)).hasRole("MEMBER")
                        .requestMatchers(AppURLs.getCombineURL(AppURLs.MANAGER_URLS, AppURLs.MANAGER_URLS_PREFIX)).hasRole("MANAGER")
                        .requestMatchers(AppURLs.getCombineURL(AppURLs.EXPERT_URLS, AppURLs.EXPERT_URLS_PREFIX)).hasRole("EXPERT")
                        .requestMatchers(AppURLs.getCombineURL(AppURLs.ADMIN_URLS, AppURLs.ADMIN_URLS_PREFIX)).hasRole("ADMIN")

                        .requestMatchers(AppURLs.MEMBER_MANAGER_URLS).hasAnyRole("MEMBER", "MANAGER")
                        .requestMatchers(AppURLs.ADMIN_EXPERT_URLS).hasAnyRole("ADMIN", "EXPERT")
                        .requestMatchers(AppURLs.MEMBER_EXPERT_URLS).hasAnyRole("MEMBER", "EXPERT")

                        .requestMatchers(AppURLs.MEMBER_MANAGER_EXPERT_URLS).hasAnyRole("MEMBER", "MANAGER", "EXPERT")
                        .requestMatchers(AppURLs.ADMIN_MANAGER_EXPERT_URLS).hasAnyRole("ADMIN", "MANAGER", "EXPERT")

                        .anyRequest().authenticated()
                )
                .formLogin(login->login.disable())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/loginForm")
                        .userInfoEndpoint(user ->
                                user.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((req, res, ex) ->
                                res.sendRedirect("/loginForm?error")
                        )
                )
                .logout(logout->logout
                        .logoutUrl("/logout")
                        .deleteCookies("token")
                        .addLogoutHandler(new CookieClearingLogoutHandler("token"))
                        .logoutSuccessUrl("/loginForm")
                )
                /**
                 * 현재 View 를 반환하는 형태 -> 데이터를 반환하는 형식으로 변경할 예정
                 */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loginAuthenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    /**
     * url반환에서 -> Rest Api 방식 반환으로 변경할 생각
     * @return
     */
    @Bean
    public AuthenticationEntryPoint loginAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/loginForm");
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }



}
