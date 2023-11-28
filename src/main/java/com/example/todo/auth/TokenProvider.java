package com.example.todo.auth;

import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import io.jsonwebtoken.Claims;
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

    /**
     * JSON Web Token을 생성하는 메서드
     * @param userEntity - 토큰의 내용(클레임)에 포함될 유저 정보
     * @return - 생성된 JSON을 암호화 한 토큰값
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
        claims.put("role", userEntity.getRole().toString());

        return Jwts.builder()
                // token header 에 들어갈 서명
                .signWith(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), // 만들어낸 시크릿키를 바이트 배열로 매개값을 줘야함.
                        SignatureAlgorithm.HS512 // 어떤 알고리즘을 통해서 서명할 것인지 선택하는 것?
                )
                // token payload에 들어갈 클레임 설정
                // 서명 -> 셋클레임 : 추가클레임을 먼저 설정하지 않으면 에러가 발생함.
                .setClaims(claims) // 나머지 기타를 넣는 방법 (Cloims 나 Map 을 이용해서 넣기!) 이러한 정보들을 claim이라고 부름
                .setIssuer("Todo운영자") // iss: 발급자 정보
                .setIssuedAt(new Date()) // iat: 발급시간
                .setExpiration(expiry) // exp: 만료시간
                // -------여기까지는 필수값세팅!(클레임스 빼고) 여기부터는 옵션! -------------------------------------------------
                .setSubject(userEntity.getId()) // sub: 토큰을 식별할 수 있는 주요 데이터 (얘는 넣는 것 권장) (매개변수값으로 서브식별값?으로 사용하겠다는 것)
                .compact();
    }

    /**
     * 클라이언트가 전송한 문자열 토큰을 디코딩하여 토큰의 위조 여부를 확인
     * 토큰을 json으로 파싱해서 클레임(토큰 정보)을 리턴
     * @param token
     * @return - 토큰 안에 있는 인증된 유저 정보를 반환
     */
    public TokenUserInfo validateAndGetTokenUserInfo(String token) {

        // 그레이들에 추가한 라이브러리를 통해 토큰을 받아낼것임
        Claims claims = Jwts.parserBuilder()
                // 토큰 발급자의 발급 당시의 서명을 넣어줌(그래서 서명이 일치하는지 비교해봐야함) (바이트배열로 달라고해서 getBytes 메서드 이용)
                // 여기서 토큰값이 위조된 경우를 체크해낼 수 잇음( 서명위조검사: 위조된 경우에는 예외가 발생)
                // 위조가 되지 않은 경우에는 payload(토큰의 바디역할을 하는 애) 를 리턴.
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.info("claims: {}", claims);

        return TokenUserInfo.builder()
                .userId(claims.getSubject()) // 위에 토큰 만든거 보면 userId는 subject에 넣어줬었음
                .email(claims.get("email", String.class))
                .role(Role.valueOf(claims.get("role", String.class)))
                .build();
    }

}
