package midas.SoundOfFlower.repository.diary;

import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.dto.response.StatisticalEmotionResponse;

import java.time.LocalDate;
import java.util.List;

public interface SearchDiary {

    List<DiaryInfoResponse> getMonthDiaryInfo(Long year, Long month, String socialId);
    DiaryInfoResponse getDayDiaryInfo(Long year, Long month, Long day, String socialId);
    List<StatisticalEmotionResponse> getStatisticalEmotion(LocalDate startDate, LocalDate endDate, String socialId);
}
