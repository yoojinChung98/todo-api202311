package com.example.todo.auth;

import com.example.todo.userapi.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component // 그냥 일반 빈등록~
@Slf4j
// 역할 : 토큰을 발급하고, 서명 위조를 검사하는 객체.
public class TokenProvider {

    @Value("${jwt.secret}") // 스프링 프레임워크의 아노테이션을 선택해야함!
    // @Value: properties 형태의 파일의 내용을 읽어서 변수에 대입하는 아노테이션. (yml도 가능)
    // 서명에 사용할 값 (512비트 이상의 랜덤 문자열 사용을 권장함.) 너무 짧다면 보안에 불리할 수 있기 때문!
    private String SECRET_KEY;
    // TodoApplicationTests 클래스에서 함 랜덤문자열 생성테스트를 해보겠음

    // 토큰 생성 메서드

    /*

     */


    public String createToken(User userEntity) {

        // 토큰 만료시간 생성
        Date expiry = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS) // now + 1 DAY  해주세여!
        );

        // 토큰 생성
        /*
            {
                "iss" : "서비스 이름(발급자)", // issuer의 줄임말, 토큰 발행자
                "exp" : "20223-12-27(서비스 만료일자)", // 우리가 만들면 되는 것임.
                "iat" : "2023-11-27(발급일자)", // issued at 의 줄임말
                "email" : "로그인한 사람의 이메일",
                "role" : "Premium"
                ...
                == 서명
            }
         */

        // 추가 클레임 정의
        Map<String, String> claims = new HashMap<>();
        claims.put("email", userEntity.getEmail());
//        claims.put("role", userEntity.getRole());

        return Jwts.builder()
                // token header 에 들어갈 서명
                .signWith(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), // 만들어낸 시크릿키를 바이트 배열로 매개값을 줘야함.
                        SignatureAlgorithm.ES512 // 어떤 알고리즘을 통해서 서명할 것인지 선택하는 것?
                )
                // token payload에 들어갈 클레임 설정
                .setIssuer("Todo운영자") // iss: 발급자 정보
                .setIssuedAt(new Date()) // iat: 발급시간
                .setExpiration(expiry) // exp: 만료시간
                // -------여기까지는 필수값세팅! 여기부터는 옵션! -------------------------------------------------
                .setSubject(userEntity.getId()) // sub: 토큰을 식별할 수 있는 주요 데이터 (얘는 넣는 것 권장) (매개변수값으로 서브식별값?으로 사용하겠다는 것)
                .setClaims() // 나머지 기타를 넣는 방법 (Cloims 나 Map 을 이용해서 넣기!)
                .compact();
    }

}
