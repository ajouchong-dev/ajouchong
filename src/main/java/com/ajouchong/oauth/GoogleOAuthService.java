package com.ajouchong.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public GoogleUserDto getUserInfo(String accessToken) {
        log.debug("Google 사용자 정보 요청 시작");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GoogleUserDto> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL, 
                HttpMethod.GET, 
                entity, 
                GoogleUserDto.class
            );
            
            GoogleUserDto userInfo = response.getBody();
            if (userInfo == null) {
                throw new OAuthException("Google API에서 사용자 정보를 받지 못했습니다.");
            }
            
            log.debug("Google 사용자 정보 조회 성공: {}", userInfo.getEmail());
            return userInfo;
            
        } catch (RestClientException e) {
            log.error("Google API 호출 실패: {}", e.getMessage());
            throw new OAuthException("Google 사용자 정보 조회에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("Google 사용자 정보 처리 중 예외 발생: {}", e.getMessage());
            throw new OAuthException("사용자 정보 처리 중 오류가 발생했습니다.", e);
        }
    }
}
