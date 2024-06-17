package org.example.newsfeed.config;

import jakarta.servlet.Filter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.newsfeed.jwt.JwtAuthenticationFilter;
import org.example.newsfeed.jwt.JwtAuthorizationFilter;
import org.example.newsfeed.jwt.JwtUtil;
import org.example.newsfeed.security.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
//        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
//        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
//        return filter;
//    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 설정
        http.csrf(csrf -> csrf.disable());

        // 세션 관리 설정
        http.sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 요청에 대한 권한 설정
        http.authorizeHttpRequests(authorizeHttpRequests ->
            authorizeHttpRequests
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 리소스 접근 허용 설정
                .requestMatchers("/api/signup").permitAll()
                .requestMatchers("/api/login").permitAll() // '/api'로 시작하는 요청 모두 접근 허가 (테스트용)
                .requestMatchers("/api/logout").permitAll()
                .requestMatchers("/api/newsfeed").permitAll()
                .anyRequest().authenticated() // 그 외 모든 요청 인증처리
        );

        // 필터 설정
        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
//        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
