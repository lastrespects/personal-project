package com.mmb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
