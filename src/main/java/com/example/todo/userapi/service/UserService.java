package com.example.todo.userapi.service;

import com.example.todo.auth.TokenProvider;
import com.example.todo.auth.TokenUserInfo;
import com.example.todo.exception.NoRegisteredArgumentsException;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserRequestSignUpDTO;
import com.example.todo.userapi.dto.response.KakaoUserDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Value("${kakao.client_id}")
    private String KAKAO_CLIENT_ID;
    @Value("${kakao.redirect_url}")
    private String KAKAO_REDIRECT_URI;
    @Value("${kakao.client_secret}")
    private String getKAKAO_CLIENT_SECRET;


    // yml 파일에 작성한 값 가져와서 사용.
    @Value("${upload.path}")
    private String uploadRootPath;

    // 회원 가입 처리
    public UserSignUpResponseDTO create(
            final UserRequestSignUpDTO dto,
            final String uploadedFilePath
    ) {
        String email = dto.getEmail();

        if(isDuplicate(email)) {
            log.warn("이메일이 중복되었습니다. - {}", email);
            throw new RuntimeException("중복된 이메일 입니다.");
        }

        // 패스워드 인코딩
        String encoded = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encoded);

        // dto를 User Entity로 변환해서 저장
        User saved = userRepository.save(dto.toEntity(uploadedFilePath));
        log.info("회원 가입 정상 수행됨! - saved user - {}", saved);

        return new UserSignUpResponseDTO(saved);

    }

    public boolean isDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 회원 인증
    public LoginResponseDTO authenticate(final LoginRequestDTO dto) {

        // 이메일을 통해 회원 정보 조회
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(
                        () -> new RuntimeException("가입된 회원이 아닙니다.")
                );

        // 패스워드 검증
        String rawPassword = dto.getPassword(); // 입력한 비번
        String encodedPassword = user.getPassword(); // DB에 저장된 암호화된 비번

        if(!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        log.info("{}님 로그인 성공!", user.getUserName());

        // 로그인 성공 후에 클라이언트에게 뭘 리턴할 것인가???
        // -> JWT를 클라이언트에게 발급해 주어야 한다!
        String token = tokenProvider.createToken(user);

        return new LoginResponseDTO(user, token);

    }


    // 프리미엄으로 등급 업
    public LoginResponseDTO promoteToPremium(TokenUserInfo userInfo) {

        User foundUser = userRepository.findById(userInfo.getUserId())
                .orElseThrow(
                        () -> new NoRegisteredArgumentsException("회원 조회에 실패했습니다!")
                );

        // 일반(COMMON) 회원이 아니라면 예외 발생
        // @PreAuthorize 아노테이션을 사용하면 이 부분 코드는 생략되어도 문제가 없음.
        if(userInfo.getRole() != Role.COMMON) {
            throw new IllegalArgumentException("일반 회원이 아니라면 등급을 상승시킬 수 없습니다.");
        }

        // 등급 변경
        foundUser.changeRole(Role.PREMIUM);
        User saved = userRepository.save(foundUser);

        // 토큰을 재발급! (새롭게 변경된 정보로)
        String token = tokenProvider.createToken(saved);

        return new LoginResponseDTO(saved, token);
    }

    /**
     * 업로드 된 파일을 서버에 저장하고 저장 경로를 리턴.
     *
     * @param profileImg - 업로드 된 파일의 정보
     * @return 실제로 저장된 이미지 경로
     */
    public String uploadProfileImage(MultipartFile profileImg) throws IOException {

        // 루트 디렉토리가 실존하는 지 확인 후 존재하지 않으면 생성.
        File rootDir = new File(uploadRootPath);
        if (!rootDir.exists()) rootDir.mkdirs();

        // 파일명을 유니크하게 변경 (이름 충돌 가능성 대비)
        // UUID 와 원본파일명을 혼합. -> 규칙없고 걍 맘대로 조합하새요~
        String uniqueFileName = UUID.randomUUID() + "_" + profileImg.getOriginalFilename();

        // 파일을 저장
        File uploadFile = new File(uploadRootPath + "/" + uniqueFileName);
        profileImg.transferTo(uploadFile); // 예외처리는 메서드호출부로 throw~

        return uniqueFileName;
    }

    public String findProfilePath(String userId) {
        User user = userRepository.findById(userId).orElseThrow();

        String profileImg = user.getProfileImg();
        if(profileImg.startsWith("http://")) {
            return profileImg;
        }
        // DB에 저장되는 profile_img는 파일명. => service가 가지고 있는 Root Path 와 연결해서 리턴
        return uploadRootPath + "/" + profileImg;
    }

    public LoginResponseDTO kakaoService(final String code) {

        // 인가코드를 통해 토큰 발급받기 (메서드화)
        Map<String, Object> responseData = getKakaoAccessToken(code);
        log.info("token: {}", responseData.get("access_token"));

        // 토큰을 통해 사용자 정보 가져오기
        KakaoUserDTO dto = getKakaoUserInfo((String)responseData.get("access_token"));

        // 일회성 로그인으로 처리 ->  dto를 바로 화면단으로 리턴 or 자체 jwt를 생성해서 리턴(이 경우 DB로 들어가야함).
        // 회원가입 처리 -> 이메일 중복 검사 진행 -> 자체 jwt를 생성해서 토큰을 화면단에 리턴. -> 화면단에서는 적절한 url을 선택하여 redirect를 진행.
        // 수업시간엔 회원가입 처리부분을 구현해본다고 함!


        if(!isDuplicate(dto.getKakaoAccount().getEmail())) { // 이메일이 중복되지 않을 때 데이터를 save 하겠다! (User 테이블에 행 집어넣어야지~)
            User saved = userRepository.save(dto.toEntity(  (String)responseData.get("access_token") ));
        } /* else { // 이메일이 중복됐다? -=> 이전에 로그인 한 적이 있다. -> DB에 데이터를 또 넣을 필요가 없다.

            // 사용자 입장에서는 로그인 했을 때, 회원가입없이 로그인이 되도록 해야함.
            // 결국 사용자 입장에서는 회원테이블에 자기 계정이 등록되던 말던 상관 없이 두 경우 모두 로그인처리가 되도록 보여야함.
            // 그래서 else 가 필요 없음
        }*/

        // 로그인 처리
        User foundUser = userRepository.findByEmail(dto.getKakaoAccount().getEmail())
                .orElseThrow();

        String token = tokenProvider.createToken(foundUser);

        // 이전에 한번 로그인 한 적있는 유저의 access_token의 값을 새로 받은 로그인토큰으로 수정.
        foundUser.setAccessToken((String)responseData.get("access_token") );
        userRepository.save(foundUser);

        return new LoginResponseDTO(foundUser, token);
    }

    private KakaoUserDTO getKakaoUserInfo(String accessToken) {

        // 요청 uri
        String requestUri = "https://kapi.kakao.com/v2/user/me";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 요청 보내기
        RestTemplate template = new RestTemplate();
        ResponseEntity<KakaoUserDTO> responseEntity
                = template.exchange(requestUri, HttpMethod.GET, new HttpEntity<>(headers), KakaoUserDTO.class);

        // 응답 바디 읽기
        KakaoUserDTO responseData = responseEntity.getBody();
        log.info("user profile: {}", responseData);

        return responseData;
    }


    private Map<String, Object> getKakaoAccessToken(String code) {

        // 요청 uri (공식 문서에서 지정해줬음)
        String requestUri = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 요청 바디(파라미터) 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); // 카카오공식 문서 기준으로 세팅
        params.add("client_id", KAKAO_CLIENT_ID); // 카카오 디벨로퍼 REST API 키
        params.add("redirect_uri", KAKAO_REDIRECT_URI); // 카카오 디벨로퍼 등록된 redirect_uri
        params.add("code", code); // 프론트에서 인가 코드 요청 시 전달받은 코드값 (매개값으로 받았음)
        params.add("client_secret", getKAKAO_CLIENT_SECRET); // 카카오 디벨로퍼 Client Secret 활성화 해놓았기 때문에 필수로 입력해야 함.

        // 헤더와 바디 정보를 합치기 위해 HttpEntity 객체 생성
        // 생성자의 매개값으로 아까 만들어둔 params 와 headers 를 집어넣어줌!
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(params, headers);// 머 담기위해 선언하는 거니까<Object>로 선언해도 문제 없음!

        // 카카오 서버로 토큰을 얻기 위한 POST 통신 (REST 방식 통신을 돕는 애 = RestTemplate)
        RestTemplate template = new RestTemplate();

        // 통신을 보내면서 응답데이터를 리턴
        // param1: 요청 url
        // param2: 요청 메서드
        // param3: 헤더와 요청파라미터정보 엔터티 (HttpEntity 타입으로 보내야함)
        // param4: 응답 데이터를 받을 객체의 타입 (ex: dto, (따로 디자인한 객체가 없다면) map(java.util))
                // 먄약 *구조가 복잡한 경우*에는 응답 데이터 타입을 String으로 받아서 JSON-simple 라이브러리로 직접 해체해도 ㄱㅊ

        // 우리가 이런 데이터를 줄테니까 응답데이터값이랑 교환하쟝~!
        ResponseEntity<Map> responseEntity
                = template.exchange(requestUri, HttpMethod.POST, requestEntity, Map.class);
        // 그냥 토큰만 꺼내서 쓸거니까 굳이 객체를 설계하지 않고 그냥 Map으로 받아냄.

        // 응답 데이터에서 필요한 정보를 가져오기
        // key 값은 보통 String 이고 value 는 다양한 형태가 올 수 있으므로 Object
        Map<String, Object> responseData = (Map<String, Object>) responseEntity.getBody();
        log.info("토큰 요청 응답 데이터: {}", responseData);

        // 응답받은 데이터묶음에서 access_token 이라는 이름의 데이터를 return (Object를 String으로 형변환 하여 리턴)
        return responseData;
    }

    public String logout(TokenUserInfo userInfo) {

        User foundUser = userRepository.findById(userInfo.getUserId())
                .orElseThrow();

        // ctrl alt v : 변수화 ctrl alt m : 메서드화
        String accessToken = foundUser.getAccessToken();
        if(accessToken != null) {

            String reqUri = "https://kapi.kakao.com/v1/user/logout";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> responseData = template.exchange(reqUri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
            return responseData.getBody();
        }

        return null;

    }
}












