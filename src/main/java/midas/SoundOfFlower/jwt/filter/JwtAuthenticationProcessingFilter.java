package midas.SoundOfFlower.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.jwt.dto.response.TokenResponse;
import midas.SoundOfFlower.jwt.error.JwtErrorHandler;
import midas.SoundOfFlower.jwt.error.TokenStatus;
import midas.SoundOfFlower.jwt.service.JwtService;
import midas.SoundOfFlower.redis.entity.BlackList;
import midas.SoundOfFlower.redis.repository.BlackListRepository;
import midas.SoundOfFlower.repository.user.UserRepository;
import midas.SoundOfFlower.service.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static midas.SoundOfFlower.error.CustomServletException.sendJsonError;
import static midas.SoundOfFlower.error.ErrorCode.*;


@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String LOGIN_CHECK_URL = "/login";

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final BlackListRepository blackListRepository;
    private final JwtErrorHandler jwtErrorHandler;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            AntPathMatcher pathMatcher = new AntPathMatcher();
            String requestURI = request.getRequestURI();

            if (requestURI.equals(LOGIN_CHECK_URL) ||pathMatcher.match("/auth/**", requestURI)){

                filterChain.doFilter(request, response);
                return;
            }

            Optional<String> accessToken = jwtService.extractAccessToken(request);
            Optional<String> refreshToken = jwtService.extractRefreshToken(request);

            if (accessToken.isEmpty()) {
                throw new CustomException(NOT_VALID_ACCESSTOKEN);
            }

            TokenStatus tokenStatus = jwtService.isTokenValid(accessToken.get());
            if (tokenStatus.equals(TokenStatus.SUCCESS)) {
                checkAccessTokenAndAuthentication(request, response, filterChain);
                return;
            }

            if (tokenStatus.equals(TokenStatus.EXPIRED) && pathMatcher.match("/token/reissue", requestURI)) {

                String socialId = request.getHeader("socialId");
                TokenResponse tokenResponse = authService.validateToken(accessToken.get(), refreshToken.get(), socialId);
                jwtService.setTokens(response, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
            } else {
                jwtErrorHandler.tokenError(tokenStatus);
            }
        } catch (CustomException e) {
            sendJsonError(response, e.getErrorCode().getStatus().value(), e.getErrorCode().getCode());
        }

    }


    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<String> accessToken = jwtService.extractAccessToken(request);

            if (accessToken.isEmpty()) {
                throw new CustomException(NOT_VALID_ACCESSTOKEN);
            }

            Optional<String> tokenSocialId = jwtService.extractSocialId(accessToken.get());
            if (tokenSocialId.isEmpty()) {
                throw new CustomException(NOT_EXTRACT_SOCIALID);
            }
            String socialID = tokenSocialId.get();
            User user = userRepository.findBySocialId(socialID)
                    .orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));
            Optional<BlackList> blackList = blackListRepository.findBySocialId(socialID);

            if (blackList.isPresent() && blackList.get().getAccessToken().equals(accessToken)) {
                throw new CustomException(NOT_VALID_ACCESSTOKEN);
            }
            saveAuthentication(user);

            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            sendJsonError(response, e.getErrorCode().getStatus().value(), e.getErrorCode().getCode());
        }
    }


    public void saveAuthentication(User user) {
        String password = user.getPassword();
        if (password == null) {
            password = UUID.randomUUID().toString().replace("-","").substring(0,8);
        }
        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(user.getSocialId())
                .password(password)
                .roles(user.getRole())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, user.getPassword(),authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}