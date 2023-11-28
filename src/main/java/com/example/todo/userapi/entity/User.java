package com.example.todo.userapi.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter @Getter
@ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "tbl_user")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id; // pk로 쓸 거고, 사용자가 입력하는 값이 아님. (계정명이 아니라 식별 코드)

    @Column(nullable = false, unique = true)
    private String email; // 이메일주소가 아이디가 되도록 할 것.

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @CreationTimestamp
    private LocalDateTime joinDate;

    @Enumerated(EnumType.STRING)
//    @ColumnDefault("'COMMON'") // ColumnDefault :디폴트값 설정, 원래는 그냥 문자열 주면 되는데, enum 타입이라서 홑따옴표를 또 넣어줌.
    @Builder.Default // enum이 안먹길래 걍 직접초기화 해버렸음
    private Role role = Role.COMMON; // 유저 권한 초기값설정

}
