package com.sparta.myselectshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sparta.myselectshop.dto.KakaoUserInfoDto;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    public String kakaoLogin(String code) throws JsonProcessingException {
        // 1. 인가 코드로 엑세스 토큰 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출: "엑세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfoDto);

        // 4. JWT 반환
        String createToken = jwtUtil.createToken(kakaoUser.getUsername(), kakaoUser.getRole());

        /* 클라이언트 쪽에서 JWT를 받아서 쿠키 저장소에 저장하고, 요청하는 api 헤더에 붙여서 올 수 있도록 하는 등의 처리가 되는데
        * 여기서는 직접 cookie를 생성해서 해야함 (Controller에서) */
        return createToken;
    }

    // 인증 코드로 토큰을 요청하려고 하는 것
    private String getToken(String code) throws JsonProcessingException {
        log.info("인가 코드: " + code);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "cc6fef87dcc02e5d4df5a69fc066168a");
        body.add("redirect_uri", "http://localhost:8080/api/user/kakao/callback");
        body.add("code", code); // 우리가 받아온 인가코드

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri) // body가 있으니 POST
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity, String.class
        );

        // HTTP 응답 (JSON) -> 엑세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText(); // 원하는 엑세스 토큰만 받아옴
    }

    // 사용자 정보 가져오기
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("인가 코드: " + accessToken);
        // 요청 URI 만들기 (정해진 path에 요청) - 요청하는 uri 니까 ..
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("v2/user/me")
                .encode()
                .build()
                .toUri();

        // Header랑 Body를 Body 하나로 보기 때문에 정보를 보내준다고 보고,
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        // Bearer이 토큰이라는 것을 알려주는 식별자 역할을 함
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri) // -> 궁금하네 ...
                .headers(headers)
                .body(new LinkedMultiValueMap<>()); // LinkedMultiValueMap<>()

        // MultiValueMap을 넣으면, 추상 클래스라 안된다 -> 그의 구현체가 LinkedMultiValueMap이고,
        // Entity에 들어갈 타입과 body의 들어갈 타입이 일치해야 한다

        // request 할 때 사용자 로그인 정보 게정이나 비밀번호를 넣어서 같이 보내줘야 하는 경우에는 body에 넣어주지만
        // 지금 요청할 때는 body에 넣지 않고 accessToken만 넣어서 보내주는 경우이기 때문에 굳이 넣을 필요는 없다..!

        // 결국 여기서 post를 쓰는 이유?
        // post를 쓰는 경우는 한 번 요청을 하고, 어떤 상태를 변화시킬 때 post 요청을 쓴다.
        // 로그인 같은 경우는 좀 애매한데, 사용자의 인증정보를 제공해서 사용자의 정보를 가져오는 행위인데
        // api 설계 상으로 봐서는 get이 맞다.
        // 하지만 이거를 post로도 지원하는 경우는, 아마 리퀘스트에 들어가는 password 때문일 것
        // requestbody에 넣어서 password를 넣어주는 경우..
        // password가 ?param에 찍히면 안되니까 ,, body에 담아서 post 요청으로 해결하는 경우가 잇다 (보안상의 이유로)
        // 로그인 api를 굳이 요청한다면 웬만하면 post 요청을 사용하기는 한다 (보안상의 이유로. password를 입력하는 경우로 !)
        // 지금 같은 경우는 access의 인증정보를 가져오는 경우이기 대문에 get을 사용해도 됨

//        RequestEntity<String> requestEntity1 = new RequestEntity<>(headers, HttpMethod.GET, uri);
//        RequestEntity<String> requestEntity2 = new RequestEntity<>(headers, HttpMethod.POST, uri);

        // HTTP
        ResponseEntity<String> response = restTemplate.exchange( // exchange는 body에도 넣는데 header에도 넣음
                requestEntity, String.class
        );

        // jsonnode로부터 데이터를 갖고오기
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();


        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new KakaoUserInfoDto(id, nickname, email);
    }

    // transactional 에너테이션을 사용하지 않았기 때문에 dirty checking 을 하지 않음
    // 상황에 따라서 어쩔 때는 update고 어쩔 때는 save이기 때문에
    // 그냥 save 메소드를 호출하는 방법이 더 효율적일 수 있음
    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        /* 우리는 기존에 있던 User 테이블을 사용할 것이기 때문에 이렇게 구현된 것
        * 만약 다르게 사용하고 싶다면 다르게 커스텀 해야함 (ex. kakaouser 테이블 생성) */

        // DB에 중복된 kakao id가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        // kakao id로 사용자를 찾고, 없으면 null로 처리 (우리 서비스에 한 번이라도 카카오 아이디로 로그인 한 사람이 없는지 !)
        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        // 만약 kakaoID로 찾은 사용자가 null , 즉 없다면 -> 회원가입을 시켜주면 되겠구나 !
        if (kakaoUser ==  null) {
            // 카카오 사용자의 email과 동일한 email을 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);

            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser; // email을 갖고있는 사용자를 찾았으니, 해당 사용자를 kakaoUser에 넣어줌
                // 기존 회원정보에 카카오 ID 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
            } else {
                // 신규 회원가입
                // password는 random한 UUID
                String password = UUID.randomUUID().toString();
                String encodedpassword = passwordEncoder.encode(password);

                // email: kakao email
                String email = kakaoUserInfo.getEmail();

                kakaoUser = new User(kakaoUserInfo.getNickname(), encodedpassword, email, UserRoleEnum.USER, kakaoId);
            }

            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    } // registerKakaoUserIfNeeded()


}

/* 일단 인가 코드를 받아와야 한다
*  인가 코드를 받으려면 일단 애플리케이션 등록을 해야 한다
*  인가 코드를 받아왔으면, 그것을 통해서 우리가 엑세스 토큰을 다시 요청을 해야 한다
*  그렇게 해서 엑세스 토큰을 받아 왔으면 그 엑세스 토큰을 가지고 다시 한 번 카카오 서버에 사용자 정보를 요청 해야 한다
*  받아온 사용자 정보를 통해서 회원가입을 시킬지, 로그인을 시킬지, 아니면 그냥 이미 회원가입 한 번 해줬네 바로 로그인! 이렇게 할지
*  제어해서 로직을 구현해 처리를 해야 한다 */

    /*
    {
      "id": 1632335751,
      "properties": {
        "nickname": "르탄이",
        "profile_image": "http://k.kakaocdn.net/...jpg",
        "thumbnail_image": "http://k.kakaocdn.net/...jpg"
      },
      "kakao_account": {
        "profile_needs_agreement": false,
        "profile": {
          "nickname": "르탄이",
          "thumbnail_image_url": "http://k.kakaocdn.net/...jpg",
          "profile_image_url": "http://k.kakaocdn.net/...jpg"
       },
        "has_email": true,
        "email_needs_agreement": false,
        "is_email_valid": true,
        "is_email_verified": true,
        "email": "letan@sparta.com"
       }
    }
    */
