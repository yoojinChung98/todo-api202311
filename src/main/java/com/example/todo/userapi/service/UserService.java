package com.example.todo.userapi.service;


import com.example.todo.userapi.dto.UserRequestSignUpDTO;
import com.example.todo.userapi.dto.UserSignUpResponseDTO;
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
}
