package com.example.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
class TodoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("토큰 서명 해시값 생성하기")
	void makeSecretKey() {

		SecureRandom random = new SecureRandom();
		byte[] key = new byte[64]; // 64byte 짜리 배열 선언 (512비트 이상의 길이를 권장한다고 했자나!)
		random.nextBytes(key); // 이 배열에 가득 랜덤 문자열을 넣어줌.
		String encodedKey = Base64.getEncoder().encodeToString(key); // 이 부분 이해 안가지만 대충 이런 코드래
		System.out.println("\n\n\\n");
		System.out.println("encodedKey = " + encodedKey);
		System.out.println("\n\n\\n");

	}


}
