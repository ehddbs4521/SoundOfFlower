package midas.SoundOfFlower.repository.diary;

import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.dto.response.StatisticalEmotionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchDiary {

    List<DiaryInfoResponse> getDiaryInfo(Long year, Long month, String socialId);
    List<StatisticalEmotionResponse> getStatisticalEmotion(LocalDateTime startDate, LocalDateTime endDate, String socialId);
}
