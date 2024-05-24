package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.request.DateRequest;
import midas.SoundOfFlower.dto.request.WriteDiaryRequest;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.dto.response.StatisticalEmotionResponse;
import midas.SoundOfFlower.entity.Diary;
import midas.SoundOfFlower.entity.DiaryImage;
import midas.SoundOfFlower.entity.Music;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.repository.diary.DiaryRepository;
import midas.SoundOfFlower.repository.diaryimage.DiaryImageRepository;
import midas.SoundOfFlower.repository.music.MusicRepository;
import midas.SoundOfFlower.repository.user.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static midas.SoundOfFlower.error.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final RestTemplate restTemplate;
    private final DiaryImageService diaryImageService;
    private final MusicLikesService musicLikesService;

    public List<DiaryInfoResponse> searchMonthDiaryInfo(Long year, Long month, String socialId) {
        return diaryRepository.getMonthDiaryInfo(year, month, socialId);
    }

    public DiaryInfoResponse searchDayDiaryInfo(Long year, Long month, Long day, String socialId) {
        return diaryRepository.getDayDiaryInfo(year, month, day, socialId);
    }

    public DiaryInfoResponse writeDiary(Long year, Long month, Long day, String socialId, WriteDiaryRequest writeDiaryRequest, List<MultipartFile> images) throws IOException {

        if (writeDiaryRequest.getTitle() == null) {
            throw new CustomException(NOT_EXIST_TITLE_DIARY);
        }

        DiaryInfoResponse diaryInfoResponse = analyzeEmotion(writeDiaryRequest.getComment());
        LocalDate LocalDate = createLocalDate(year, month, day);
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));
        Music music = musicRepository.findBySpotify(diaryInfoResponse.getSpotify())
                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_SPOTIFY));


        Diary diary = getDiary(writeDiaryRequest, diaryInfoResponse, LocalDate, user, music);
        diary.setUser(user);

        diaryRepository.save(diary);

        List<String> imageUrls = null;

        if (images != null || images.size() != 0) {
            imageUrls = diaryImageService.uploadDiaryImages(images);
            diaryInfoResponse.updateImgUrl(imageUrls);

            for (String url : imageUrls) {
                DiaryImage diaryImage = DiaryImage.builder().url(url).build();
                diaryImage.setDiary(diary);
                diaryImageRepository.save(diaryImage);
            }
        }

        return diaryInfoResponse;
    }

    private DiaryInfoResponse analyzeEmotion(String comment) {
        try {
            String url = "http://localhost:8000/analyze/emotion";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("comment", comment);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestMap, headers);
            return restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
        } catch (Exception e) {
            throw new CustomException(EXTERNAL_API_FAILURE);
        }
    }

    private Diary getDiary(WriteDiaryRequest writeDiaryRequest,
                           DiaryInfoResponse diaryInfoResponse,
                           LocalDate LocalDate,
                           User user,
                           Music music) {

        return Diary.builder()
                .title(writeDiaryRequest.getTitle())
                .comment(writeDiaryRequest.getComment())
                .date(LocalDate)
                .flower(diaryInfoResponse.getFlower())
                .angry(diaryInfoResponse.getAngry())
                .sad(diaryInfoResponse.getSad())
                .delight(diaryInfoResponse.getDelight())
                .calm(diaryInfoResponse.getCalm())
                .embarrased(diaryInfoResponse.getEmbarrased())
                .anxiety(diaryInfoResponse.getAnxiety())
                .love(diaryInfoResponse.getLove())
                .user(user)
                .music(music)
                .build();
    }

    public List<StatisticalEmotionResponse> getStatisticalEmotion(DateRequest dateRequest, String socialId) {

        LocalDate startDate = createLocalDate(dateRequest.getStartYear(), dateRequest.getStartMonth(), dateRequest.getStartDay());
        LocalDate endDate = createLocalDate(dateRequest.getEndYear(), dateRequest.getEndMonth(), dateRequest.getEndDay());

        List<StatisticalEmotionResponse> statisticalEmotion = diaryRepository.getStatisticalEmotion(startDate, endDate, socialId);

        return statisticalEmotion;
    }

    public LocalDate createLocalDate(Long year, Long month, Long day) {
        return LocalDate.of(year.intValue(), month.intValue(), day.intValue());
    }

    @Transactional
    public DiaryInfoResponse modifyDiary(Long year, Long month, Long day, String socialId, WriteDiaryRequest writeDiaryRequest, List<MultipartFile> images) throws IOException {

        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));

        Diary diary = user.findDiaryByDate(year, month, day);
        DiaryInfoResponse diaryInfoResponse = null;

        if (writeDiaryRequest.getTitle() != null) {
            diary.updateTitle(writeDiaryRequest.getTitle());
            diaryRepository.save(diary);
        }

        if (writeDiaryRequest.getComment() != null) {
            diaryInfoResponse = analyzeEmotion(writeDiaryRequest.getComment());
            diary.updateComment(writeDiaryRequest.getComment());
            diary.updateEmotion(diaryInfoResponse.getAngry(),
                    diaryInfoResponse.getSad(),
                    diaryInfoResponse.getDelight(),
                    diaryInfoResponse.getCalm(),
                    diaryInfoResponse.getEmbarrased(),
                    diaryInfoResponse.getAnxiety(),
                    diaryInfoResponse.getLove());
            diary.updateFlower(diaryInfoResponse.getFlower());

            Music music = musicRepository.findBySpotify(diaryInfoResponse.getSpotify())
                    .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_SPOTIFY));
            diary.setMusic(music);
            diary.setUser(user);

            diaryRepository.save(diary);
        }

        if (images != null) {
            List<DiaryImage> diaryImages = diary.getImageUrls();
            diary.getImageUrls().clear();
            List<String> imageUrls = diaryImageService.updateImageUrls(diaryImages, images);
            diaryInfoResponse.updateImgUrl(imageUrls);

            for (String url : imageUrls) {
                DiaryImage diaryImage = DiaryImage.builder().url(url).build();
                diaryImage.setDiary(diary);
                diaryImageRepository.save(diaryImage);
            }
        }
        if (diaryInfoResponse != null) {

            boolean likes = musicLikesService.isLikes(diaryInfoResponse.getSpotify(), socialId);
            diaryInfoResponse.updateLike(likes);
        }



        return diaryInfoResponse;
    }

    public void deleteDiary(Long year, Long month, Long day, String socialId) {
        diaryRepository.deleteDiary(year, month, day, socialId);
    }
}
