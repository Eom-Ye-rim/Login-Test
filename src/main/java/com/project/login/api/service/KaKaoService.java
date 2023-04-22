package com.project.login.api.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.nimbusds.jose.shaded.json.parser.ParseException;
import com.project.login.api.entity.OAuthToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// https://kauth.kakao.com/oauth/authorize?client_id=179011b75542e1a21fa2207d50a4df57&redirect_uri=http://localhost:8080/auth/kakao/callback&response_type=code
@Service
public class KaKaoService {
    public String getAccessToken(String code) throws IOException {

//		// POST 방식으로 key=value 데이터를 요청 (카카오쪽으로)
//		// 이 때 필요한 라이브러리가 RestTemplate, 얘를 쓰면 http 요청을 편하게 할 수 있다.
        RestTemplate rt = new RestTemplate();
//
//		// HTTP POST를 요청할 때 보내는 데이터(body)를 설명해주는 헤더도 만들어 같이 보내줘야 한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
//
//		// body 데이터를 담을 오브젝트인 MultiValueMap를 만들어보자
//		// body는 보통 key, value의 쌍으로 이루어지기 때문에 자바에서 제공해주는 MultiValueMap 타입을 사용한다.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "179011b75542e1a21fa2207d50a4df57");
        params.add("redirect_uri", "http://localhost:8080/auth/kakao/callback");
        params.add("code", code);
//
//		// 요청하기 위해 헤더(Header)와 데이터(Body)를 합친다.
//		// kakaoTokenRequest는 데이터(Body)와 헤더(Header)를 Entity가 된다.
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // POST 방식으로 Http 요청한다. 그리고 response 변수의 응답 받는다.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // https://{요청할 서버 주소}
                HttpMethod.POST, // 요청할 방식
                kakaoTokenRequest, // 요청할 때 보낼 데이터
                String.class
                // 요청 시 반환되는 데이터 타입
        );
        System.out.println("응답" + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;

        try {
            oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.print("Accesstoken:" + oAuthToken.getAccess_token());
        System.out.print("Accesstoken:" + oAuthToken.getRefresh_token());
        //return "카카오 토큰 요청 완료 : 토큰 요청에 대한 응답 : "+response;
        return oAuthToken.getAccess_token();
    }

//    public String getUserInfo(String accessToken) throws IOException {
//        HashMap<String, Object> userInfo = new HashMap<String, Object>();
//        RestTemplate rt = new RestTemplate();
////
////		// HTTP POST를 요청할 때 보내는 데이터(body)를 설명해주는 헤더도 만들어 같이 보내줘야 한다.
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + accessToken);
//        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
////
////		// body 데이터를 담을 오브젝트인 MultiValueMap를 만들어보자
////		// body는 보통 key, value의 쌍으로 이루어지기 때문에 자바에서 제공해주는 MultiValueMap 타입을 사용한다.
////
////		// 요청하기 위해 헤더(Header)와 데이터(Body)를 합친다.
////		// kakaoTokenRequest는 데이터(Body)와 헤더(Header)를 Entity가 된다.
//        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);
//
//        // POST 방식으로 Http 요청한다. 그리고 response 변수의 응답 받는다.
//        ResponseEntity<String> response = rt.exchange(
//                "https://kapi.kakao.com/v2/user/me", // https://{요청할 서버 주소}
//                HttpMethod.POST, // 요청할 방식
//                kakaoProfileRequest, // 요청할 때 보낼 데이터
//                String.class
//                // 요청 시 반환되는 데이터 타입
//        );
//        System.out.println(response);
//
//
////			String profile_nickname = response.getBody("nickname").getAsString();
////			String account_email = response.getBody("email").getAsString();
////
////			userInfo.put("profile_nickname", profile_nickname);
////			userInfo.put("account_email", account_email);
//////
//        return response.getBody();
//    }
//}

    public Map<String, Object> getUserInfo(String access_token) throws IOException {
        String host = "https://kapi.kakao.com/v2/user/me";
        Map<String, Object> result = new HashMap<>();
        try {
            URL url = new URL(host);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + access_token);
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);


            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String res = "";
            while ((line = br.readLine()) != null) {
                res += line;
            }

            System.out.println("res = " + res);


            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(res);
            JSONObject kakao_account = (JSONObject) obj.get("kakao_account");
            JSONObject properties = (JSONObject) obj.get("properties");


            String id = obj.get("id").toString();
            String nickname = properties.get("nickname").toString();
            String email = kakao_account.get("email").toString();

            result.put("id", id);
            result.put("nickname", nickname);
            result.put("email", email);

            br.close();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}