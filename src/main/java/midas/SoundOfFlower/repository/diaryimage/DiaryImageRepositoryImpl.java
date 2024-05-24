package midas.SoundOfFlower.repository.diaryimage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.entity.Diary;
import midas.SoundOfFlower.entity.DiaryImage;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static midas.SoundOfFlower.entity.QDiary.diary;
import static midas.SoundOfFlower.entity.QDiaryImage.diaryImage;

@Slf4j
public class DiaryImageRepositoryImpl implements DeleteDiaryImage {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public DiaryImageRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Transactional
    @Override
    public void deleteDiaryImage(List<DiaryImage> diaryImages) {
        for (DiaryImage diaryImage : diaryImages) {
            em.remove(diaryImage);
        }
        em.flush();
        em.clear();
    }
}
