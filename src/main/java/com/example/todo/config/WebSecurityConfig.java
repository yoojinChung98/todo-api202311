package com.example.todo.config;

import com.example.todo.filter.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

// 스프링 부트에서는 이런 설정 파일(config...)은 xml로 작성하는 것이 아닌 아노테이션을 하나 달아주면 된다!
//@Configuration // 설정 클래스 용도로 사용하도록 스프링에 등록하는 아노테이션
@EnableWebSecurity // 시큐리티 설정 파일로 사용할 클래스 선언. (얘를 통해서 기본적으로 설정된 것들을 꺼버리겠음)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;

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
                .httpBasic().disable()
        //http의 cors 랑 csrf를 꺼주시구요, httpBasic(기본적인보안설정)도 꺼주세요!
                .sessionManagement() // 세션 인증을 사용하지 않겠다는 뜻! (스프링시큐리티가 지 멋대로 세션인증을 쓰기 때문에 차단해놓는 것)
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 이제 여기부터는 어떤 요청에서 인증을 안할 것인지 설정. 어떤 요청에서 인증을 할 것인지도 설정.
                .authorizeRequests()
                // "/api/auth"로 시작하는 요청과 "/" 요청은 권한 설정 없이 허용하겠다.
                .antMatchers("/", "/api/auth/**").permitAll() // 다음과 같은 요청은 필터적용없이 통과시키겠다!
//                .antMatchers(HttpMethod.POST, "/api/todos").hasRole("ADMIN").permitALL() // 이런 식으로 검사도 가능~ ('/api/todos' POST 요청이 들어오고, ROLE 값이 ADMIN인 경우! 권한검시 없이 허용하겠다.)
                .anyRequest().authenticated() // 위에서 설정하지 않은 나머지 요청은 모두 인증(권한검사가 필요함, 필터를 거쳐야)이 되어야 한다. (즉, todos로 들어오는 모든 요청은 권한검사를 하겠다는 의미)
                ;

        // 토큰 인증 필터 연결을 해야해욥!
        // jwtAuthFilter 먼저 연결을 하고 그 다음 순서로 CORS 필터를 이후에 통과하도록 설정해야만 함!
        // 권한이 있는지 먼저 파악하고 그다음에 크로스 오리진이 잘 설정되어있는지 체크해야하는 것임.
        http.addFilterAfter(
                jwtAuthFilter,
                CorsFilter.class // import 주의! 스프링꺼로 해야함!!
        );

        return http.build(); // 그리고 기본보안설정 없는 새로운 http 생성해서 주겟음.
    }

}
