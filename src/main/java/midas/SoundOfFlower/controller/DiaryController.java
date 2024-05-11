package midas.SoundOfFlower.controller;

import lombok.RequiredArgsConstructor;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/diary")
@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/calendar/{month}")
    public ResponseEntity<Object> calendalInfo(@PathVariable Long month) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        DiaryInfoResponse diaryInfoResponse = diaryService.searchDiaryInfo(month, socialId);

        return ResponseEntity.ok(diaryInfoResponse);
    }
}
