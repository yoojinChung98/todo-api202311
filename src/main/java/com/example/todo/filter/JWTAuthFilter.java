package com.example.todo.filter;


// 클라이언트가 전송한 토큰을 검사하는 필터 : 토큰 값이 유효한가에 따라서 디스패처가 해당 요청을 해결할 수 있는 메서드를 찾는 과정을 시작할 필요가 없을 수도 있음.
// 디스패처 서블릿이 요청을 받기 전에 토큰의 상태에 따라서 필터링을 하고 요청자체가 발생하지 않도록 할 수도 있음.
// import / 상속/ 구현 등의 방법으로 필터 클래스를 사용 할 수 있음.

import com.example.todo.auth.TokenProvider;
import com.example.todo.auth.TokenUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    // 요청마다 한번씩 동작하게 하는 필터! 를 상속. (스프링 프레임워크에서 제공하는 클래스)

    private final TokenProvider tokenProvider;

    // 필터가 해야 할 작업을 기술
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // filterChain : 필터를 통과하게 할 건지, 걸리게 할 것인지 정할 때 사용하는 객체

        try{
            String token = parseBearerToken(request);
            log.info("JWT Token Filter is running... -token: {}", token);

            // 토큰 위조검사 및 인증완료 처리. (어떤 메서드가 어떤 상황에 사용되는가를 눈여겨 볼 것)
            if(token != null) { // 간단한 널체크
                // 토큰 서명 위조 검사와 토큰을 파싱해서 클레임을 얻어내는 작업 (메서드화 시킬거고, 토큰관련 파일에 모아 작성하는 것이 좋을 듯)
                TokenUserInfo userInfo = tokenProvider.validateAndGetTokenUserInfo(token);

                // 인가 정보 리스트
                List<SimpleGrantedAuthority> authorityList = new ArrayList<>(); // 권한이 여러 가지일 경우 리스트로 권한 체크에 사용할 필드를 add
                // (우리의 경우는 굳이,, Role 타입의 필드 하나만으로 권한을 체크하기 때문에 하나만 Add..... 리스트가 아니어도 상관은 없었지만 함 보라고 일케 만들어주심)
                authorityList.add(new SimpleGrantedAuthority(userInfo.getRole().toString())); // 매개값은 문자열로 주면 됨.

                // 인증 완료 처리
                // (스프링 시큐리티에게 인증정보를 전달해서 전역적으로 어플리케이션에서
                // 인증 정보를 활용할 수 있게 설정)
                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userInfo, // 컨트롤러에서 활용할 유저 정보
                        null, // 인증된 사용자의 비밀번호 - 보통 null 값을 사용함 (왜냐하면 토큰에 진짜 비밀번호값을 넣는 것이 흔치 않기 때문)
                        authorityList // 인가 정보 (=권한 정보)
                );

                // 인증 완료 처리 시 클라이언트의 요청 정보 세팅
                // 요청을 보냈던 사람의 정보를 저장해놓고, 그 사람이 나쁜 사람이었으면 기억해두었다가 미리 차단할 수도 있고, 만약 문제가 없는 정보였다면 문제 없이 통과할 수 있도록 함.
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 스프링 시큐리티 컨테이너에 인증 정보 객체를 등록.
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("토큰이 위조되었습니다.");
        }

        // 필터 체인에 내가 만든 필터를 실행하라는 명령 (해당 요청을 필터를 통과하게 할것이나/ 아니면 걸러지게 할것이냐!를 설정하는 객체)
        filterChain.doFilter(request, response);

    }

    private String parseBearerToken(HttpServletRequest request) {
        // 요청 헤더에서 토큰 꺼내오기를 담당할 메서드
        // -- content-type : application/json
        // -- Authorization : Bearer dfdfei2jiejfiosdj893ur2u38r...(토큰값)

        String bearerToken = request.getHeader("Authorization"); // 헤더에 담긴 특정 정보 가저오기

        // 요청 헤더에서 가져온 토큰은 순수 토큰 값이 아닌 Bearer 접두어가 붙어있는 값. 이것을 제거하는 작업.
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;

    }


}
