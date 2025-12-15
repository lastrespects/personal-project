package com.mmb.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import com.mmb.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> memberRepository.findByUsername(username)
                .map(member -> {
                    Integer authLevel = member.getAuthLevel();
                    return User.builder()
                            .username(member.getUsername())
                            .password(member.getPassword())
                            .roles(authLevel != null && authLevel == 0 ? "ADMIN" : "USER")
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                     PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    DaoAuthenticationProvider authenticationProvider) throws Exception {

        http.authenticationProvider(authenticationProvider);

        http.authorizeHttpRequests(auth -> auth
                // ✅ 1) Spring Boot 표준 static 경로(css/js/images/webjars/favicon 등) 전부 허용
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                // ✅ 2) 네가 static 루트에 둔 mmb.css 직접 허용 (http://localhost:8081/mmb.css)
                .requestMatchers("/mmb.css").permitAll()

                // ✅ 3) 기존 permitAll 목록 유지
                .requestMatchers(
                        "/login",
                        "/usr/member/login",
                        "/usr/member/join",
                        "/usr/member/doJoin",
                        "/usr/member/validLoginInfo",
                        "/usr/member/restore",
                        "/usr/member/checkUsername",
                        "/usr/member/checkNickname",
                        "/usr/member/findLoginId",
                        "/usr/member/doFindLoginId",
                        "/usr/member/findLoginPw",
                        "/usr/member/doFindLoginPw",
                        "/doLogin",
                        "/logout",
                        "/usr/home/**",
                        "/view/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico"
                ).permitAll()

                .anyRequest().authenticated()
        );

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (isAjaxOrJson(request)) {
                        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "F-401", "로그인이 필요합니다.");
                        return;
                    }
                    response.sendRedirect("/usr/member/login?error=1");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (isAjaxOrJson(request)) {
                        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "F-403", "권한이 없습니다.");
                        return;
                    }
                    response.sendRedirect("/usr/member/login?error=1");
                })
        );

        http.csrf(csrf -> csrf.disable());

        http.headers(headers -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                )));

        http.formLogin(form -> form
                .loginPage("/usr/member/login")
                .loginProcessingUrl("/doLogin")
                .defaultSuccessUrl("/usr/home/main", true)
                .failureUrl("/usr/member/login?error=1")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/usr/home/main")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeJsonError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write("{\"resultCode\":\"" + code + "\",\"msg\":\"" + message + "\"}");
        response.getWriter().flush();
    }

    private boolean isAjaxOrJson(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String xrw = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");

        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        boolean wantsJson = accept != null && accept.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);

        return ajax || wantsJson;
    }
}
