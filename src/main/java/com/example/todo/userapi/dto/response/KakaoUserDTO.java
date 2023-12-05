package com.example.todo.userapi.dto.response;

import com.example.todo.userapi.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter @Setter
@ToString
public class KakaoUserDTO {

    private long id;

    @JsonProperty("connected_at")
    private LocalDateTime connectedAt; // 카멜케이스로 작성

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount; // 묶음이므로 클래스로 받을 거임.

    @Setter @Getter @ToString
    public static class KakaoAccount {
        // 나중에 KakaoUserDTO 인스턴스가 삭제될 때, GarbageCollector가 KakaoAccount 를 인식할 수 없기 때문에(참조하고 있는 필드가 없음)
        // 아예 static으로 선언해서 걍 해당 클래스와의 연관을 약간 끊어내서? GC가 KakaoAccount가 사용되지 않는다고 인식할 수 있도록(삭제를 위해) 만든 것?

        private String email;
        private Profile profile;

        @Getter @Setter @ToString
        public static class Profile {
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }

    public User toEntity(String accessToken) {
        return User.builder()
                .email(this.kakaoAccount.email)
                .userName(this.kakaoAccount.profile.nickname)
                .password("password!")
                .profileImg(this.kakaoAccount.profile.profileImageUrl)
                .accessToken(accessToken)
                .build();
    }
}
