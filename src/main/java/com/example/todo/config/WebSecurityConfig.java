package com.example.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 스프링 부트에서는 이런 설정 파일(config...)은 xml로 작성하는 것이 아닌 아노테이션을 하나 달아주면 된다!
@Configuration // 설정 클래스 용도로 사용하도록 스프링에 등록하는 아노테이션
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
