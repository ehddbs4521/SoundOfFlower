package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.repository.DiaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public List<DiaryInfoResponse> searchDiaryInfo(Long month, String socialId) {

        List<DiaryInfoResponse> diaryInfo = diaryRepository.getDiaryInfo(month, socialId);

        return diaryInfo;
    }
}
