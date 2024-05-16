package midas.SoundOfFlower.repository.diary;

import midas.SoundOfFlower.dto.response.DiaryInfoResponse;

import java.util.List;

public interface SearchDiary {

    List<DiaryInfoResponse> getDiaryInfo(Long year, Long month, String socialId);
}
