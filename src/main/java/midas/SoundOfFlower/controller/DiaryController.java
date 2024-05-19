package midas.SoundOfFlower.controller;

import lombok.RequiredArgsConstructor;
import midas.SoundOfFlower.dto.request.WriteDiaryRequest;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.service.DiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/diary")
@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/calendar")
    public ResponseEntity<Object> calendalInfo(@RequestParam Long year, @RequestParam Long month) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        List<DiaryInfoResponse> diaryInfoResponses = diaryService.searchDiaryInfo(year, month, socialId);

        return ResponseEntity.ok(diaryInfoResponses);
    }

    @PostMapping("/calendar")
    public ResponseEntity<Object> writeDiary(@RequestParam Long year,
                                             @RequestParam Long month,
                                             @RequestParam Long day,
                                             @RequestPart(value = "comment") WriteDiaryRequest writeDiaryRequest,
                                             @RequestPart(value = "images") List<MultipartFile> images) throws IOException {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        DiaryInfoResponse diaryInfoResponse = diaryService.writeDiary(year, month, day, socialId, writeDiaryRequest,images);

        return ResponseEntity.ok(diaryInfoResponse);
    }

    @PutMapping("/calendar")
    public ResponseEntity<Object> modifyDiary(@RequestParam Long year,
                                              @RequestParam Long month,
                                              @RequestParam Long day,
                                              @RequestBody WriteDiaryRequest writeDiaryRequest,
                                              @RequestPart(value = "images") List<MultipartFile> images) throws IOException {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        DiaryInfoResponse diaryInfoResponse = diaryService.modifyDiary(year, month, day, socialId, writeDiaryRequest, images);

        return ResponseEntity.ok(diaryInfoResponse);
    }

    @DeleteMapping("/calendar")
    public ResponseEntity<Object> deleteDiary(@RequestParam Long year,
                                              @RequestParam Long month,
                                              @RequestParam Long day) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        diaryService.deleteDiary(year, month, day, socialId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
