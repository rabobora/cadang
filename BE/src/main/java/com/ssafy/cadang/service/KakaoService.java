package com.ssafy.cadang.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.cadang.domain.User;
import com.ssafy.cadang.dto.KakaoInfo;
import com.ssafy.cadang.dto.KakaoToken;
import com.ssafy.cadang.jwt.JwtToken;
import com.ssafy.cadang.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Date;

@Service
@RequiredArgsConstructor
//@Transactional
public class KakaoService {

    private final UserRepository userRepository;

    @Value("${spring.registration.kakao.client-id}")
    private String client_id;

    @Value("${spring.registration.kakao.client-secret}")
    private String client_secret;

    @Value("${spring.registration.kakao.redirect-uri}")
    private String redirect_uri;

    @Value("${spring.provider.kakao.token-uri}")
    private String token_uri;

    @Value("${spring.provider.kakao.user-info-uri}")
    private String user_info_uri;

    @Value("${spring.jwt.secretKey}")
    private String secretKey;


    // 인가코드를 사용해서 카카오에게 엑세스 토큰 요청하기
    public KakaoToken getAccessToken(String code) {

        // 요청 param ( body )
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client_id);
        params.add("redirect_uri", redirect_uri);
        params.add("code", code);
        params.add("client_secret", client_secret);

        //request
        WebClient wc = WebClient.create(token_uri);
        String response = wc.post()
                .uri(token_uri)
                .body(BodyInserters.fromFormData(params))
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8") //요청 헤더
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //json형태로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoToken = null;

        try {
            kakaoToken = objectMapper.readValue(response, KakaoToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoToken;
    }


    // 카카오로부터 받은 엑세스 토큰을 사용하여 사용자 정보 가져오기
    public KakaoInfo requestInfo(String access_token) {

        System.out.println(" requestInfo / 정보 가져오기 ");

        // HTTP 요청
        WebClient wc = WebClient.create(user_info_uri);

        System.out.println(" requestInfo / webclient 시작 ");

        String response = wc.post()
                .uri(user_info_uri)
                .header("Authorization", "Bearer " + access_token)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8") // 요청 헤더
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(" requestInfo / response ");


        ObjectMapper objectMapper = new ObjectMapper();
        KakaoInfo userinfo = null;

        System.out.println(" requestInfo / 정보담기 ");


        try {

            System.out.println(" requestInfo / try response ");

            userinfo = objectMapper.readValue(response, KakaoInfo.class);
        } catch (JsonProcessingException e) {

            System.out.println(" requestInfo / catch ");

            e.printStackTrace();
        }

        System.out.println(" requestInfo / 성공 "+userinfo);

        return userinfo;
    }


    // 카카오 로그인 회원가입 + 사용자 정보 가져오기
    @Transactional
    public User addUser(KakaoToken token) {

        System.out.println("addUser / 사용자 정보 가져오기");

        KakaoInfo info = requestInfo(token.getAccess_token()); // 사용자 정보 가져오기

        System.out.println(" addUser / 회원 확인하기 ");
        System.out.println("사용자 회원 번호 : " + info.getId());
        User user = userRepository.findByUserId(info.getId()); // 가입된 회원인지 확인하기


        // 최초 연동시 회원가입
        if (user == null) {

            System.out.println(" addUser / 없는 회원이므로 회원가입 ");

            // 닉네임 ( 임시 )
            String nickname = String.valueOf((int) (Math.random() * 900000) + 100000);

            // 유저 정보 입력
            user = User.builder()
                    .userId(info.getId())
                    .userName(nickname)
//                    .userBirth(Integer.parseInt(info.getKakao_account().getBirthyear()))
//                    .userGender(gender)
                    .userCaffeine(400) // 성인 기준
                    .userSugar(50) // 20대 성인 여성 기준
                    .registerDatetime(LocalDate.now())
                    .kakaoRefreshToken(token.getRefresh_token()) // 카카오 리프레시 토큰 저장
//                    .jwtRefreshToken()
                    .build();

            System.out.println(" addUser / 회원 정보담기");


            userRepository.save(user);
            System.out.println(" addUser / 회원 추가하기 ");

        } else {

            // jwt 토큰 발급하고 jwt refresh 토큰 저장
            System.out.println("jwt 발급해보자");
            JwtToken jwtToken = getJwtToken(user);

            // 유저 기존 정보 수정하고 save
            user.setJwtRefreshToken(jwtToken.getRefreshToken());

            userRepository.save(user);
        }

        System.out.println(" addUser / 성공 ");

        return user;
    }

    // JWT 토큰 발급
    public JwtToken getJwtToken(User addUser) { // jwttoken 최초 발급

        System.out.println("getJwt / 들어옴 ");
        // jwtAccessToken 발급
        String jwtAccessToken = Jwts.builder()
                .setHeaderParam("type", "jwt") //Header 설정부분
                .setHeaderParam("alg", "HS256") //Header 설정부분
                .claim("userId", addUser.getUserId()) // Payload 설정부분
                .claim("userName", addUser.getUserName()) // Payload 설정부분
                .setExpiration(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 60 * 4))) // 만료시간 : 4시간
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        System.out.println("jwt / 엑세스 ");

        // jwtRefreshToken 발급
        String jwtRefreshToken = Jwts.builder()
                .setHeaderParam("type", "jwt") //Header 설정부분
                .setHeaderParam("alg", "HS256") //Header 설정부분
                .claim("userId", addUser.getUserId()) // Payload 설정부분
                .setExpiration(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 60 * 24 * 30))) // 만료시간 : 30일
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        System.out.println("jwt / 리프레시");



        return new JwtToken(jwtAccessToken, jwtRefreshToken);

    }
}