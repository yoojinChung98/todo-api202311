package com.example.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 스프링 부트에서는 이런 설정 파일(config...)은 xml로 작성하는 것이 아닌 아노테이션을 하나 달아주면 된다!
//@Configuration // 설정 클래스 용도로 사용하도록 스프링에 등록하는 아노테이션
@EnableWebSecurity // 시큐리티 설정 파일로 사용할 클래스 선언. (얘를 통해서 기본적으로 설정된 것들을 꺼버리겠음)
public class WebSecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 시큐리티 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Security 모듈이 기본적으로 제공하는 보안 정책 해제.
        http
                .cors()
                .and()
                .csrf().disable()
                .httpBasic().disable();
        //http의 cors 랑 csrf를 꺼주시구요, httpBasic(기본적인보안설정)도 꺼주세요!

        return http.build(); // 그리고 기본보안설정 없는 새로운 http 생성해서 주겟음.
    }

}
