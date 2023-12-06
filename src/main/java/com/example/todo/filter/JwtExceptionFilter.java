package com.example.todo.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        try {
            // 예외가 발생하지 않으면 Auth Filter 로 통과 ~
            filterChain.doFilter(request, response); // 필터를 통과하는 로직
            // 필터의 배치는 어떤 식으로 진행할 것이냐? WebSecurityConfig로!

        } catch (JwtException e) {
            // 토큰이 만료되었을 시 Auth Filter에서 예외를 강제 발생 -> 앞에 배치한 Exception Filter(여기 클래스) 로 예외가 전달됨)
            log.info("만료 예외 발생! - {}", e.getMessage());
            setErrorResponse(response, e); // 메서드화 하려고 함.
        }


    }

    private void setErrorResponse(HttpServletResponse response, JwtException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8"); // JSON 데이터 내부에 한글이 들어올 것 같다면 이렇게 charset 설정 해야함.

        // Map 생성 및 데이터 추가
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", e.getMessage());
        responseMap.put("code", 401);
        
        // Map을 JSON 문자열로 변환
        String jsonString = new ObjectMapper().writeValueAsString(responseMap); // Object 객체를 문자열로!
        
        response.getWriter().write(jsonString); // 문자열로 변환한 JSON 문자열을 읽어들어 응답겍체에 실음 (그 응답객체가 브라우저로 바로 응답될 것임.)
    }
}
