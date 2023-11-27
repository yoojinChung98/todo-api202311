package com.example.todo.userapi.dto.request;

import com.example.todo.userapi.entity.User;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter @Getter
@ToString @EqualsAndHashCode(of = "email")
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserRequestSignUpDTO {

    @NotBlank
    @Email // 해당 값이 이메일형식을 띄고 있는지 검증해주는 애인가봄! 머 골벵이가 들어갔는지,, 등등
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    @Size(min = 2, max = 6)
    private String userName;

    // DTO를 Entity로 변경하는 메서드
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .password(this.password)
                .userName(this.userName)
                .build();
    }

}
