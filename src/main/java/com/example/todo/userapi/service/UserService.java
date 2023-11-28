package com.example.todo.userapi.service;


import com.example.todo.auth.TokenProvider;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserRequestSignUpDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // 빈 컨테이너에 등록된 빈 중에, 반환타입이 PsswordEncoder 인 빈이 하나 있어서 해당 메서드가 작동?돼서? 새로 생성된 BCryptPasswordEncoder가 주입됨? 맞는 듯
    private final TokenProvider tokenProvider; // 서비스에서 주입받아서 사용하려구 TokenProvider 클래스를 빈등록(component) 해놓은 것.

    // 회원 가입 처리
    public UserSignUpResponseDTO create(final UserRequestSignUpDTO dto) {
        String email = dto.getEmail();

        if(isDuplicate(email)) {
            log.warn("이메일이 중복되었습니다. - {}",  email);
            throw new RuntimeException("중복된 이메일 입니다.");
        }

        // 패스워드 인코딩 (그리고 dto에 재셋팅)
        String encoded = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encoded);

        // dto를 User Entity로 변환해서 저장
        User saved = userRepository.save(dto.toEntity());
        log.info("회원 가입 정상 수행됨! - saved user - {}", saved);

        // 지금 당장은 비워두고, 나중에 화면단 에서 회원가입 직후 머 회원가입정보를 넘겨달라!
        // 머 이런 각 상황에 따라서 그 때 추가하면 됨!
        // 일단은 머 대충 먼가를 반환을 시켜주긴 할거임
        return new UserSignUpResponseDTO(saved);
    }


    public boolean isDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 회원 인증
    public LoginResponseDTO authenticate(final LoginRequestDTO dto) {

        // 이메일을 통해 회원정보 조회 먼저!
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(
                        () -> new RuntimeException("가입된 회원이 아닙니다.")
                );

        // 패스워드 검증
        String rawPassword = dto.getPassword(); // 입력한 비번
        String encodedPassword = user.getPassword(); // DB에 저장된 암호화된 비번.
        log.info(rawPassword);

        if(!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        log.info("{}님 로그인 성공!", user.getUserName());

        // 로그인 성공 후에 클라이언트에게 뭘 리턴할 것인가???
        // // -> JWT !를 클라이언트에게 발급해 줄 것.
        String token = tokenProvider.createToken(user);// user정보를 바탕으로 토큰 하나를 생성해조!

        return new LoginResponseDTO(user, token);


    }
}
