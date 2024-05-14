package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.request.WriteDiaryRequest;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.entity.Diary;
import midas.SoundOfFlower.entity.Music;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.repository.DiaryRepository;
import midas.SoundOfFlower.repository.MusicRepository;
import midas.SoundOfFlower.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static midas.SoundOfFlower.error.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final RestTemplate restTemplate;

    public List<DiaryInfoResponse> searchDiaryInfo(Long year, Long month, String socialId) {

        List<DiaryInfoResponse> diaryInfo = diaryRepository.getDiaryInfo(year, month, socialId);

        return diaryInfo;
    }

    public DiaryInfoResponse writeDiary(Long year, Long month, Long day, String socialId, WriteDiaryRequest writeDiaryRequest) {
        DiaryInfoResponse diaryInfoResponse = null;
        try {
            String url = "http://localhost:8000/analyze/emotion";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            WriteDiaryRequest request = new WriteDiaryRequest(writeDiaryRequest.getContent());
            HttpEntity<WriteDiaryRequest> requestEntity = new HttpEntity<>(request, headers);
            diaryInfoResponse = restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
        } catch (CustomException e) {

            throw new CustomException(EXTERNAL_API_FAILURE);
        }

        LocalDateTime localDateTime = createLocalDateTime(year, month, day);
        User user = userRepository.findBySocialId(socialId).orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));

        Music music = musicRepository.findByMusicId(diaryInfoResponse.getMusicId()).orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

        Diary diary = Diary.builder()
                .comment(writeDiaryRequest.getContent())
                .date(localDateTime)
                .flower("장미")
                .angry(diaryInfoResponse.getAngry())
                .sad(diaryInfoResponse.getSad())
                .delight(diaryInfoResponse.getDelight())
                .calm(diaryInfoResponse.getCalm())
                .embarrased(diaryInfoResponse.getEmbarrased())
                .anxiety(diaryInfoResponse.getAnxiety())
                .user(user)
                .music(music)
                .build();

        diary.setUser(user);

        diaryRepository.save(diary);

        return diaryInfoResponse;
    }

    public LocalDateTime createLocalDateTime(Long year, Long month, Long day) {

        return LocalDateTime.of(year.intValue(), month.intValue(), day.intValue(), 0, 0);
    }

    public DiaryInfoResponse writeTest(String content) {

        DiaryInfoResponse diaryInfoResponse = null;

        try {
            String url = "http://localhost:8000/test";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            WriteDiaryRequest request = new WriteDiaryRequest(content);
            HttpEntity<WriteDiaryRequest> requestEntity = new HttpEntity<>(request, headers);
            diaryInfoResponse = restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
            return diaryInfoResponse;
        } catch (CustomException e) {

            throw new CustomException(EXTERNAL_API_FAILURE);
        }
    }
}
