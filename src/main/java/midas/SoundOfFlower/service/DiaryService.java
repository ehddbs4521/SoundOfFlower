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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    public List<DiaryInfoResponse> searchDiaryInfo(Long year, Long month, String socialId) {
        return diaryRepository.getDiaryInfo(year, month, socialId);
    }

    public DiaryInfoResponse writeDiary(Long year, Long month, Long day, String socialId, WriteDiaryRequest writeDiaryRequest, List<MultipartFile> images) throws IOException {

        if (writeDiaryRequest.getTitle() == null) {
            throw new CustomException(NOT_EXIST_TITLE_DIARY);
        }

        DiaryInfoResponse diaryInfoResponse = analyzeEmotion(writeDiaryRequest);

        LocalDateTime localDateTime = createLocalDateTime(year, month, day);
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));
        Music music = musicRepository.findByMusicId(diaryInfoResponse.getMusicId())
                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));


        Diary diary = getDiary(writeDiaryRequest, diaryInfoResponse, localDateTime, user, music);
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
        boolean likes = musicLikesService.isLikes(diaryInfoResponse.getMusicId(), socialId);

        diaryInfoResponse.updateLike(likes);

        return diaryInfoResponse;
    }

    private DiaryInfoResponse analyzeEmotion(WriteDiaryRequest writeDiaryRequest) {
        try {
            String url = "http://localhost:8000/analyze/emotion";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WriteDiaryRequest> requestEntity = new HttpEntity<>(writeDiaryRequest, headers);
            return restTemplate.postForObject(url, requestEntity, DiaryInfoResponse.class);
        } catch (CustomException e) {
            throw new CustomException(EXTERNAL_API_FAILURE);
        }
    }

    private Diary getDiary(WriteDiaryRequest writeDiaryRequest,
                           DiaryInfoResponse diaryInfoResponse,
                           LocalDateTime localDateTime,
                           User user,
                           Music music) {

        return Diary.builder()
                .title(writeDiaryRequest.getTitle())
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
    }

    public List<StatisticalEmotionResponse> getStatisticalEmotion(DateRequest dateRequest, String socialId) {

        LocalDateTime startDate = createLocalDateTime(dateRequest.getStartYear(), dateRequest.getStartMonth(), dateRequest.getStartDay());
        LocalDateTime endDate = createLocalDateTime(dateRequest.getEndYear(), dateRequest.getEndMonth(), dateRequest.getEndDay());

        List<StatisticalEmotionResponse> statisticalEmotion = diaryRepository.getStatisticalEmotion(startDate, endDate, socialId);

        return statisticalEmotion;
    }

    public LocalDateTime createLocalDateTime(Long year, Long month, Long day) {
        return LocalDateTime.of(year.intValue(), month.intValue(), day.intValue(), 0, 0);
    }

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
            diaryInfoResponse = analyzeEmotion(writeDiaryRequest);
            diary.updateComment(writeDiaryRequest.getComment());
            diary.updateEmotion(diaryInfoResponse.getAngry(),
                    diaryInfoResponse.getSad(),
                    diaryInfoResponse.getDelight(),
                    diaryInfoResponse.getCalm(),
                    diaryInfoResponse.getEmbarrased(),
                    diaryInfoResponse.getAnxiety(),
                    diaryInfoResponse.getLove());
            diary.updateFlower(diaryInfoResponse.getFlower());

            Music music = musicRepository.findByMusicId(diaryInfoResponse.getMusicId())
                    .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

            diary.updateMusicInfo(music);
            diary.setUser(user);

            diaryRepository.save(diary);
        }

        if (images.size() != 0) {
            List<String> imageUrls = diaryImageService.updateImageUrls(diary.getId(), images);
            diaryInfoResponse.updateImgUrl(imageUrls);

            for (String url : imageUrls) {
                DiaryImage diaryImage = DiaryImage.builder().url(url).build();
                diaryImage.setDiary(diary);
                diaryImageRepository.save(diaryImage);
            }
        }

        boolean likes = musicLikesService.isLikes(diaryInfoResponse.getMusicId(), socialId);

        diaryInfoResponse.updateLike(likes);

        return diaryInfoResponse;
    }

    public void deleteDiary(Long year, Long month, Long day, String socialId) {
        diaryRepository.deleteDiary(year, month, day, socialId);
    }
}
