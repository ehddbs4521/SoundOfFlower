package midas.SoundOfFlower.jwt.service;


import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import midas.SoundOfFlower.jwt.error.TokenStatus;
import midas.SoundOfFlower.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Getter
@Slf4j
public class JwtService {

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String SOCIAL_TYPE = "socialType";
    private static final String SOCIAL_ID = "socialId";
    private static final String BEARER = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 2;            // 유효기간 2시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14;  // 유효기간 14일

    private String accessHeader;
    private String refreshHeader;
    private final SecretKey key;
    private final UserRepository userRepository;

    public JwtService(@Value("${jwt.secret-key}") String secret,
                      @Value("${jwt.access-header}") String accessHeader,
                      @Value("${jwt.refresh-header}") String refreshHeader,
                      UserRepository userRepository) {
        this.accessHeader = accessHeader;
        this.refreshHeader = refreshHeader;
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(UTF_8));
        this.key = new SecretKeySpec(keyBytes, "HmacSHA512");
        this.userRepository = userRepository;
    }
    public String generateAccessToken(String socialId) {

        long now = (new Date()).getTime();

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder().subject(ACCESS_TOKEN_SUBJECT)
                .claim(SOCIAL_ID, socialId).expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String socialId) {

        long now = (new Date()).getTime();

        Date refreshTokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        return  Jwts.builder().subject(REFRESH_TOKEN_SUBJECT)
                .claim(SOCIAL_ID, socialId).expiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .get(EMAIL_CLAIM, String.class)
            );
        } catch (Exception e) {
            log.error("액세스 토큰이 유효하지 않습니다.");
            return Optional.empty();
        }
    }

    public Date extractTime(String accessToken) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getExpiration();
    }


    public TokenStatus isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return TokenStatus.SUCCESS;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            return TokenStatus.WRONG_SIGNATURE;
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            return TokenStatus.UNSUPPORTED;
        } catch (IllegalArgumentException e) {
            return TokenStatus.ILLEGAL_TOKEN;
        }
    }

    public Optional<String> extractSocialId(String token) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(SOCIAL_ID, String.class)
            );
        } catch (Exception e) {
            log.error("액세스 토큰이 유효하지 않습니다.");
            return Optional.empty();
        }
    }

    public void setTokens(HttpServletResponse response, String accessToken, String refreshToken) {

        response.setHeader(accessHeader, BEARER + accessToken);
        response.setHeader(refreshHeader, BEARER + refreshToken);
        response.setStatus(HttpStatus.OK.value());
    }

}

