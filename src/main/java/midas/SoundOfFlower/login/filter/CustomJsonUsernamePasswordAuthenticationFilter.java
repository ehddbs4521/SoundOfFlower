package midas.SoundOfFlower.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.repository.user.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static midas.SoundOfFlower.error.CustomServletException.sendJsonError;
import static midas.SoundOfFlower.error.ErrorCode.NOT_EQUAL_JSON;

@Slf4j
public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/auth/token/login";
    private static final String ADMIN_LOGIN_REQUEST_URL = "/auth/admin/token/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private static final String USERNAME_KEY = "email";
    private static final String SOCIAL_KEY = "socialType";
    private static final String PASSWORD_KEY = "password";
    private static final String ROLE = "role";
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);
    private static final AntPathRequestMatcher ADMIN_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(ADMIN_LOGIN_REQUEST_URL, HTTP_METHOD);

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, UserRepository userRepository) {
        super(new OrRequestMatcher(DEFAULT_LOGIN_PATH_REQUEST_MATCHER, ADMIN_LOGIN_PATH_REQUEST_MATCHER));
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        try {
            if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
                throw new CustomException(NOT_EQUAL_JSON);
            }

            String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

            Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);

            String email = usernamePasswordMap.get(USERNAME_KEY);
            String password = usernamePasswordMap.get(PASSWORD_KEY);
            String socialType = usernamePasswordMap.get(SOCIAL_KEY);
            String role = usernamePasswordMap.get(ROLE);

            User user = userRepository.findBySocialTypeAndEmailAndRole(socialType, email, role).orElseThrow(() -> new CustomException(NOT_EQUAL_JSON));

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user.getSocialId(), password);

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (CustomException e) {
            sendJsonError(response, e.getErrorCode().getStatus().value(), e.getErrorCode().getCode());
            return null;
        }

    }
}
