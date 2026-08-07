package com.unimal.admin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                // 정적 리소스는 인증 밖에 둔다. 막아두면 로그인 화면이 스타일 없이
                // 뜬다 — CSS 요청이 로그인 페이지로 리다이렉트되기 때문이다.
                it.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    .requestMatchers("/login", "/admin/**").permitAll()
                    .requestMatchers("/admin-members/**").hasRole("SUPER_ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin {
                it.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/members", true)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout {
                it.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
