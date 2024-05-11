package midas.SoundOfFlower.repository;

import midas.SoundOfFlower.dto.response.DiaryInfoResponse;

public interface SearchDiary {

    DiaryInfoResponse getDiaryInfo(Long month, String socialId);
}
