package com.example.demo.config;

import com.example.demo.filter.JwtAuthenticationFilter;
import com.example.demo.oauth.CustomOAuth2UserService;
import com.example.demo.oauth.OAuth2LoginSuccessHandler;
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/loginForm", "/joinForm", "/join", "/login",
                                "/oauth2/login", "/api/check-phone","/managerJoinForm", "/expertJoinForm").permitAll()
                        .requestMatchers("/updateRole/**").hasAnyRole("MEMBER","ADMIN","EXPERT","MANAGER")
                        .requestMatchers("/member/**").hasAnyRole("MEMBER","ADMIN")
                        .requestMatchers("/expert/**").hasRole("EXPERT")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
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
                 * 현재 View 를 반환하는 형태 -> REST API Json 데이터를 반환하는 형식으로 변경할 예정
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
