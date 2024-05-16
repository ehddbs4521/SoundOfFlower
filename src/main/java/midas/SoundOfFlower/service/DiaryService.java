package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.request.WriteDiaryRequest;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.entity.Diary;
import midas.SoundOfFlower.entity.Music;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.repository.diary.DiaryRepository;
import midas.SoundOfFlower.repository.music.MusicRepository;
import midas.SoundOfFlower.repository.user.UserRepository;
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
            WriteDiaryRequest request = new WriteDiaryRequest(writeDiaryRequest.getComment());
            HttpEntity<WriteDiaryRequest> requestEntity = new HttpEntity<>(request, headers);
            diaryInfoResponse = restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
        } catch (CustomException e) {

            throw new CustomException(EXTERNAL_API_FAILURE);
        }

        LocalDateTime localDateTime = createLocalDateTime(year, month, day);
        User user = userRepository.findBySocialId(socialId).orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));

        Music music = musicRepository.findByMusicId(1L).orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

        Diary diary = Diary.builder()
                .comment(writeDiaryRequest.getComment())
                .date(localDateTime)
                .flower(diaryInfoResponse.getFlower())
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

    public DiaryInfoResponse modifyDiary(Long year, Long month, Long day, String socialId, WriteDiaryRequest writeDiaryRequest) {
        DiaryInfoResponse diaryInfoResponse = null;
        try {
            String url = "http://localhost:8000/analyze/emotion";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            WriteDiaryRequest request = new WriteDiaryRequest(writeDiaryRequest.getComment());
            HttpEntity<WriteDiaryRequest> requestEntity = new HttpEntity<>(request, headers);
            diaryInfoResponse = restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
        } catch (CustomException e) {

            throw new CustomException(EXTERNAL_API_FAILURE);
        }

        User user = userRepository.findBySocialId(socialId).orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));

        Diary diary = user.findDiaryByDate(year, month, day);

        diary.updateComent(writeDiaryRequest.getComment());
        diary.updateEmotion(diaryInfoResponse.getAngry(),
                diaryInfoResponse.getSad(),
                diaryInfoResponse.getDelight(),
                diaryInfoResponse.getCalm(),
                diaryInfoResponse.getEmbarrased(),
                diaryInfoResponse.getAnxiety());
        diary.updateFlower(diaryInfoResponse.getFlower());

        Music music = musicRepository.findByMusicId(diaryInfoResponse.getMusicId())
                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

        diary.updateMusicInfo(music);

        diary.setUser(user);

        diaryRepository.save(diary);

        DiaryInfoResponse updatedDiaryInfo = diary.toDiaryInfoResponse();

        return updatedDiaryInfo;
    }

    public void deleteDiary(Long year, Long month, Long day, String socialId) {

        diaryRepository.deleteDiary(year, month, day, socialId);

    }
}
