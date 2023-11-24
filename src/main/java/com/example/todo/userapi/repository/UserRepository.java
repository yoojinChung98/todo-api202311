package com.example.todo.userapi.repository;

import com.example.todo.userapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // 우리는 id 가 그,,, 그냥 우리만 알고 쓸 식별키구,, 진짜 사용자가 입력할 아이디는 이메일이니까,, 이건 jpa 가 기본제공 안해!

    // 이메일로 회원 정보 조회
    Optional<User> findByEmail(String email); // 쿼리메서드 쓰면 되징~

    // 이메 중복 체크 (중복되면 회원가입 불가!)
    // JPA 사용한다면 네이티브쿼리보단 'JPQL' 사용을 추천!(엔터티에 집중할 수 있으니까!)
//    @Query("SELECT COUNT(*) FROM User u WHERE u.email =: email")
    boolean existsByEmail(String email); // 이거도,, 쿼리메서드래,,, 쌤이,,, 다,,, 속였어,,,ㅜ

}
