package midas.SoundOfFlower.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.request.DateRequest;
import midas.SoundOfFlower.dto.request.NickNameRequest;
import midas.SoundOfFlower.dto.request.ValidateNickNameRequest;
import midas.SoundOfFlower.dto.response.StatisticalEmotionResponse;
import midas.SoundOfFlower.service.AuthService;
import midas.SoundOfFlower.service.DiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final AuthService authService;
    private final DiaryService diaryService;

    @PutMapping("/reset/nickname")
    public ResponseEntity<Object> changeNickName(@RequestBody ValidateNickNameRequest validateNickNameRequest) {

        authService.changeNickName(validateNickNameRequest);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reset/profile")
    public ResponseEntity<Object> changeProfile(@RequestPart(value = "nickName") NickNameRequest nickNameRequest, @RequestPart(value = "images") MultipartFile multipartFile) throws IOException {

        String updateProfileUrl = authService.updateProfileUrl(multipartFile, nickNameRequest.getNickName());
        Map<String, String> profile = new HashMap<>();
        profile.put("url", updateProfileUrl);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/statistic/emotion")
    public ResponseEntity<Object> getStatisticalEmotion(@RequestBody DateRequest dateRequest) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        List<StatisticalEmotionResponse> statisticalEmotion = diaryService.getStatisticalEmotion(dateRequest, socialId);

        return ResponseEntity.ok(statisticalEmotion);
    }

}
