package me.lee.springbootdeveloper.config.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.lee.springbootdeveloper.util.CookieUtil;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

public class OAuth2AuthorizationRequestBasedOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // OAuth 2.0 권한 부여 요청을 쿠키로 저장하기 위한 이름으로 사용 L
    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // 쿠키의 만료 시간을 초 단위로 나타냄 L
    private final static int COOKIE_EXPIRE_SECONDS = 18000;

    // 클라이언트로부터 받은 HTTP 요청에서 OAuth 2.0 권한 부여 요청을 로드합니다.
    // 이를 위해 OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME 쿠키를 검색하고,
    // 해당 쿠키의 값을 역직렬화하여 OAuth2AuthorizationRequest 객체로 반환 L
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

        if (cookie != null) {
            // 쿠키 값이 있는 경우, 권한 부여 요청을 역직렬화하여 반환
            return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
        } else {
            // 쿠키 값이 없는 경우, 로그인 페이지로 리다이렉션
            try {
                HttpServletResponse response = (HttpServletResponse) request.getAttribute("response");
                response.sendRedirect("/login"); // 로그인 페이지로 이동
            } catch (IOException e) {
                // 리다이렉션 실패 처리
                e.printStackTrace();
            }

            return null; // 또는 다른 특정 값
        }
    }

    //  메서드는 OAuth 2.0 권한 부여 요청을 저장합니다. 만약 authorizationRequest가 null이라면,
    //  즉 권한 부여 요청이 없다면, 관련된 쿠키를 삭제합니다.
    //  그렇지 않다면, authorizationRequest 객체를 직렬화한 후 OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME 쿠키를 생성하여 응답에 추가 L
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    }

    //OAuth 2.0 권한 부여 요청을 제거합니다.
    // 이 메서드는 loadAuthorizationRequest(request) 메서드를 호출하여 쿠키를 로드한 뒤,
    // 해당 쿠키를 삭제하고 로드된 권한 부여 요청을 반환 L
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    // 메서드는 권한 부여 요청과 관련된 쿠키를 삭제합니다.
    // 이를 위해 OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME 쿠키를 삭제 L
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
