package me.lee.springbootdeveloper.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.lee.springbootdeveloper.config.jwt.TokenProvider;
import me.lee.springbootdeveloper.domain.RefreshToken;
import me.lee.springbootdeveloper.domain.User;
import me.lee.springbootdeveloper.repository.RefreshTokenRepository;
import me.lee.springbootdeveloper.service.UserService;
import me.lee.springbootdeveloper.util.CookieUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 리프레스 토큰을 저장할때 사용할 쿠키 이름 설정 L
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    // 리프레시 토큰 유효 기간 L
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    // 엑세스 토큰 유효 기간 L
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    // 인증 성공 시 리다리렉션 되는 경로 L
    public static final String REDIRECT_PATH = "/articles";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;


    // 성공적으로 인증된 후 실행되는 메서드 L
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = userService.findByEmail((String)oAuth2User.getAttributes().get("email"));

        // 1. 리프레시 토큰 생성 -> 저장 -> 쿠키에 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);
        // 2.엑세스 토큰 생성 -> 패스에 엑세스 토큰 추가
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String targetUrl = getTargetUrl(accessToken);
        // 3. 인증 관련 설정값, 쿠키 제거
        clearAuthenticationAttributes(request, response);
        // 4. 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 생성된 리프레시 토큰을 전달받아 데이터베이스에 저장
    private void saveRefreshToken(Long userId, String newRefreshToken){
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    // 생성된 리프레시 토큰을 쿠키에 저장
    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken){
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    // 인증 관련 설정값, 쿠키 제거
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response){
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 액세스 토큰 패스에 추가
    private String getTargetUrl(String token){
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
