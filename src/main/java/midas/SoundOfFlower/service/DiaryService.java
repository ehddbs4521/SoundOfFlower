package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.repository.DiaryRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryInfoResponse searchDiaryInfo(Long month, String socialId) {

        DiaryInfoResponse diaryInfo = diaryRepository.getDiaryInfo(month,socialId);

        return diaryInfo;
    }
}
