package midas.SoundOfFlower.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.request.*;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.jwt.dto.response.TokenResponse;
import midas.SoundOfFlower.jwt.error.JwtErrorHandler;
import midas.SoundOfFlower.jwt.error.TokenStatus;
import midas.SoundOfFlower.jwt.service.JwtService;
import midas.SoundOfFlower.redis.entity.BlackList;
import midas.SoundOfFlower.redis.entity.EmailAuthentication;
import midas.SoundOfFlower.redis.entity.RefreshToken;
import midas.SoundOfFlower.redis.repository.BlackListRepository;
import midas.SoundOfFlower.redis.repository.EmailAuthenticationRepository;
import midas.SoundOfFlower.redis.repository.RefreshTokenRepository;
import midas.SoundOfFlower.repository.user.UserRepository;
import midas.SoundOfFlower.util.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static midas.SoundOfFlower.entity.Role.ADMIN;
import static midas.SoundOfFlower.entity.Role.USER;
import static midas.SoundOfFlower.error.ErrorCode.*;
import static midas.SoundOfFlower.jwt.error.TokenStatus.EXPIRED;
import static midas.SoundOfFlower.oauth.dto.SocialType.SoundOfFlower;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BlackListRepository blackListRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailAuthenticationRepository emailAuthenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailUtil emailUtil;
    private final AmazonS3Client amazonS3Client;
    private final JwtService jwtService;
    private final JwtErrorHandler jwtErrorHandler;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${default.profile}")
    private String defaultProfile;

    @Value("${admin.code}")
    private String adminCode;

    @Transactional
    public void sendEmail(EmailRequest emailRequest) {

        validateEmail(emailRequest);

        String randomNum = String.valueOf((new Random().nextInt(9000) + 1000));
        long expireTime = LocalDateTime.now().plusMinutes(10)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();

        emailUtil.sendEmail(emailRequest.getEmail(), randomNum);

        String id = emailRequest.getEmail() + "_" + emailRequest.getEmailType() + "_" + emailRequest.getSocialType();
        emailAuthenticationRepository.save(new EmailAuthentication(id, randomNum, expireTime));
    }

    public void validateEmail(EmailRequest emailRequest) {
        if (userRepository.existsByEmailAndSocialType(emailRequest.getEmail(),emailRequest.getSocialType()) && emailRequest.getEmailType().equals("sign-up")) {
            throw new CustomException(EXIST_USER_EMAIL_SOCIALTYPE);
        }
        else if (!userRepository.existsByEmailAndSocialType(emailRequest.getEmail(),emailRequest.getSocialType()) && emailRequest.getEmailType().equals("reset-password")) {
            throw new CustomException(NOT_EXIST_USER_EMAIL_SOCIALTYPE);
        }
    }

    @Transactional
    public void verifyNickName(String nickName) {
        userRepository.deleteByEmailWhereIsNull();
        if (userRepository.existsByNickName(nickName)) {
            throw new CustomException(EXIST_USER_NICKNAME);
        }
        User user=User.builder()
                .nickName(nickName)
                .role(USER.getKey())
                .build();

        userRepository.save(user);
    }


    public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        String id = verifyEmailRequest.getEmail() + "_" + verifyEmailRequest.getEmailType() + "_" + verifyEmailRequest.getSocialType();

        if (userRepository.existsByEmailAndSocialType(verifyEmailRequest.getEmail(), verifyEmailRequest.getSocialType())
                && verifyEmailRequest.getEmailType().equals("sign-up")) {
            throw new CustomException(EXIST_USER_EMAIL_SOCIALTYPE);
        }
        else if (!userRepository.existsByEmailAndSocialType(verifyEmailRequest.getEmail(), verifyEmailRequest.getSocialType())
                && verifyEmailRequest.getEmailType().equals("reset-password")) {
            throw new CustomException(NOT_EXIST_USER_EMAIL);
        }
        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findById(id).orElseThrow(() -> new CustomException(NOT_EXIST_USER_EMAIL));
        if (!emailAuthentication.getRandomNum()
                .equals(verifyEmailRequest.getInputNum())) {
            throw new CustomException(WRONG_CERTIFISATION_NUMBER);
        }
        if (emailAuthentication.getExp()< Instant.now().getEpochSecond()) {
            throw new CustomException(EXPIRE_CERTIFISATION_NUMBER);
        }
    }

    @Transactional
    public void signup(String email, String pw, MultipartFile multipartFile, String nickName) throws IOException {
        String socialId = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
        String password = passwordEncoder.encode(pw);
        String url;

        url = createProfileUrl(multipartFile);

        User user = userRepository.findByNickName(nickName).orElseThrow(()->new CustomException(EXIST_USER_NICKNAME));
        user.updateAll(email, password, socialId,url,SoundOfFlower.getKey());
        userRepository.saveAndFlush(user);
    }

    private String createProfileUrl(MultipartFile multipartFile) throws IOException {
        String url;
        if (multipartFile==null) {
            url = defaultProfile;
        } else {
            String fileName = multipartFile.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            amazonS3Client.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
            url = amazonS3Client.getUrl(bucket, fileName).toString();
        }
        return url;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

        if (!resetPasswordRequest.getPassword().equals(resetPasswordRequest.getRePassword())) {
            throw new CustomException(WRONG_PASSWORD);
        }

        if (!resetPasswordRequest.getSocialType().equals(SoundOfFlower.getKey())) {
            throw new CustomException(NOT_SoundOfFlower_SOCIALTYPE);
        }

        userRepository.findBySocialTypeAndEmailAndRole(resetPasswordRequest.getSocialType(),resetPasswordRequest.getEmail(),USER.getKey())
                .ifPresentOrElse(user -> {
                    user.updatePassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
                    userRepository.save(user);
                },() -> new CustomException(NOT_EXIST_USER_EMAIL_SOCIALTYPE));
    }

    @Transactional
    public TokenResponse validateToken(String accessToken, String refreshToken, String accessTokenSocialId) {

        TokenStatus tokenValid = jwtService.isTokenValid(refreshToken);

        if (tokenValid.equals(EXPIRED)) {
            jwtErrorHandler.tokenError(tokenValid);
        }
        if (blackListRepository.existsByAccessToken(accessToken)) {
            throw new CustomException(EXIST_ACCESSTOKEN_BLACKLIST);
        }

        String newAccessToken = jwtService.generateAccessToken(accessTokenSocialId);
        String newRefreshToken = jwtService.generateRefreshToken(accessTokenSocialId);

        RefreshToken token = refreshTokenRepository.findBySocialId(accessTokenSocialId).orElseThrow(() -> new CustomException(NOT_EXIST_REFRESHTOKEN));
        token.updateRefreshToken(newRefreshToken);

        refreshTokenRepository.save(token);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return tokenResponse;
    }

    @Transactional
    public void changeNickName(ValidateNickNameRequest validateNickNameRequest) {
        Optional<User> existNickName = userRepository.findByNickName(validateNickNameRequest.getPresentNickName());
        Optional<User> changeNickName = userRepository.findByNickName(validateNickNameRequest.getChangeNickName());

        if (existNickName.isEmpty()) {
            throw new CustomException(NOT_EXIST_USER_NICKNAME);
        }
        if (changeNickName.isEmpty()) {
            User user = existNickName.get();
            user.updateNickname(validateNickNameRequest.getChangeNickName());
            userRepository.save(user);

            return;
        }

        throw new CustomException(EXIST_USER_NICKNAME);

    }

    @Transactional
    public void logout(String accessToken, String socialId) {

        if (!userRepository.existsBySocialId(socialId)) {
            throw new CustomException(NOT_EXIST_USER_SOCIALID);
        }


        TokenStatus tokenValid = jwtService.isTokenValid(accessToken);
        if (!tokenValid.equals(TokenStatus.SUCCESS)) {
            jwtErrorHandler.tokenError(tokenValid);
        }

        Optional<String> token = jwtService.extractSocialId(accessToken);
        if (token.isEmpty()) {
            throw new CustomException(NOT_EXTRACT_SOCIALID);
        }
        String tokenSocialId = token.get();

        if (!tokenSocialId.equals(socialId)) {
            throw new CustomException(NOT_EQUAL_EACH_TOKEN_SOCIALID);
        }

        if (blackListRepository.existsByAccessToken(accessToken)) {
            throw new CustomException(EXIST_ACCESSTOKEN_BLACKLIST);
        }

        RefreshToken refreshToken = refreshTokenRepository.findBySocialId(tokenSocialId).orElseThrow(() -> new CustomException(NOT_EXIST_REFRESHTOKEN));

        refreshTokenRepository.delete(refreshToken);
        Long leftTime = calculateTimeLeft(accessToken);
        blackListRepository.save(new BlackList(socialId, accessToken, leftTime));

    }

    public Long calculateTimeLeft(String accessToken) {
        Instant expirationTime = jwtService.extractTime(accessToken).toInstant();
        Instant now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        return Duration.between(now, expirationTime).getSeconds();
    }

    @Transactional
    public String updateProfileUrl(MultipartFile multipartFile,String nickName) throws IOException {

        User user = userRepository.findByNickName(nickName).orElseThrow(() -> new CustomException(NOT_EXIST_USER_NICKNAME));

        String fileName = multipartFile.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());
        amazonS3Client.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
        String url = amazonS3Client.getUrl(bucket, fileName).toString();

        user.updateProfile(url);
        userRepository.save(user);

        return url;
    }

    @Transactional
    public void adminSignUp(AdminUserRequest adminUserRequest) {

        if (!adminUserRequest.getAdminCode().equals(adminCode)) {
            throw new CustomException(WRONG_ADMIN_CODE);
        }

        if (userRepository.existsByEmailAndRole(adminUserRequest.getEmail(), ADMIN.getKey())) {
            throw new CustomException(EXIST_ADMIN_EMAIL);
        }

        String socialId = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
        String nickName = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
        String password = passwordEncoder.encode(adminUserRequest.getPassword());

        User user = User.builder()
                .email(adminUserRequest.getEmail())
                .password(password)
                .nickName(nickName)
                .socialId(socialId)
                .imageUrl(defaultProfile)
                .socialType(SoundOfFlower.getKey())
                .role(ADMIN.getKey())
                .build();

        userRepository.save(user);
    }


}
