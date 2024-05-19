package midas.SoundOfFlower.repository.diaryimage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import static midas.SoundOfFlower.entity.QDiary.diary;
import static midas.SoundOfFlower.entity.QDiaryImage.diaryImage;

public class DiaryImageRepositoryImpl implements DeleteDiaryImage {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public DiaryImageRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void deleteDiaryImage(Long diaryId) {
        queryFactory
                .delete(diaryImage)
                .where(diary.id.eq(diaryId))
                .execute();

        em.flush();
        em.clear();
    }
}
