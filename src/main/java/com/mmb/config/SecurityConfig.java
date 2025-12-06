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
                                        System.out.println("[UserDetailsService] username=" + username + ", password="
                                                        + member.getPassword());
                                        return User.builder()
                                                        .username(member.getUsername())
                                                        .password(member.getPassword())
                                                        .roles(member.getAuthLevel() != null
                                                                        && member.getAuthLevel() == 0 ? "ADMIN" : "USER")
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
        SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider)
                        throws Exception {
                http
                                .authenticationProvider(authenticationProvider)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/**").permitAll())
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers
                                                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                // align with JSP form action
                                                .loginProcessingUrl("/doLogin")
                                                .defaultSuccessUrl("/usr/home/main", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/usr/home/main")
                                                .invalidateHttpSession(true));

                return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
