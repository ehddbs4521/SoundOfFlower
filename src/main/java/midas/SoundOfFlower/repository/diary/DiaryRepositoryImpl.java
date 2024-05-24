package midas.SoundOfFlower.repository.diary;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;
import midas.SoundOfFlower.dto.response.StatisticalEmotionResponse;
import midas.SoundOfFlower.entity.Diary;
import midas.SoundOfFlower.entity.QDiaryImage;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static midas.SoundOfFlower.entity.QDiary.diary;
import static midas.SoundOfFlower.entity.QDiaryImage.diaryImage;
import static midas.SoundOfFlower.entity.QMusicLike.musicLike;
import static org.springframework.util.StringUtils.isEmpty;

public class DiaryRepositoryImpl implements SearchDiary, DeleteDiary {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public DiaryRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<DiaryInfoResponse> getMonthDiaryInfo(Long year, Long month, String socialId) {
        List<DiaryInfoResponse> diaryInfoList = queryFactory
                .select(Projections.fields(DiaryInfoResponse.class,
                        diary.id.as("diaryId"),
                        diary.flower,
                        diary.angry,
                        diary.sad,
                        diary.delight,
                        diary.calm,
                        diary.embarrased,
                        diary.anxiety,
                        diary.love,
                        diary.music.spotify,
                        musicLike.isLike.as("isLike")
                ))
                .from(diary)
                .leftJoin(musicLike)
                .on(diary.music.spotify.eq(musicLike.music.spotify)
                        .and(musicLike.user.socialId.eq(socialId)))
                .where(yearEq(year), monthEq(month), socialIdEq(socialId))
                .fetch();

        List<Tuple> diaryImages = queryFactory
                .select(diaryImage.diary.id, diaryImage.url)
                .from(diaryImage)
                .where(diaryImage.diary.id.in(diaryInfoList.stream().map(DiaryInfoResponse::getDiaryId).collect(Collectors.toList())))
                .fetch();

        Map<Long, List<String>> diaryImageMap = diaryImages.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(diaryImage.diary.id),
                        Collectors.mapping(tuple -> tuple.get(diaryImage.url), Collectors.toList())
                ));

        diaryInfoList.forEach(diaryInfo -> diaryInfo.setImgUrl(diaryImageMap.get(diaryInfo.getDiaryId())));

        return diaryInfoList;
    }

    @Override
    public DiaryInfoResponse getDayDiaryInfo(Long year, Long month, Long day, String socialId) {

        DiaryInfoResponse diaryInfoResponse = queryFactory
                .select(Projections.fields(DiaryInfoResponse.class,
                        diary.id.as("diaryId"),
                        diary.flower,
                        diary.angry,
                        diary.sad,
                        diary.delight,
                        diary.calm,
                        diary.embarrased,
                        diary.anxiety,
                        diary.love,
                        diary.music.spotify,
                        musicLike.isLike.as("isLike")
                ))
                .from(diary)
                .leftJoin(musicLike)
                .on(diary.music.spotify.eq(musicLike.music.spotify)
                        .and(musicLike.user.socialId.eq(socialId)))
                .where(yearEq(year), monthEq(month), dayEq(day), socialIdEq(socialId))
                .fetchFirst();

        if (diaryInfoResponse != null) {
            List<String> diaryImageUrls = queryFactory
                    .select(diaryImage.url)
                    .from(diaryImage)
                    .where(diaryImage.diary.id.eq(diaryInfoResponse.getDiaryId()))
                    .fetch();

            diaryInfoResponse.setImgUrl(diaryImageUrls);
        }

        return diaryInfoResponse;
    }

    @Override
    public List<StatisticalEmotionResponse> getStatisticalEmotion(LocalDate startDate, LocalDate endDate, String socialId) {
        return queryFactory
                .select(Projections.fields(StatisticalEmotionResponse.class,
                        diary.angry,
                        diary.sad,
                        diary.delight,
                        diary.calm,
                        diary.embarrased,
                        diary.anxiety))
                .from(diary)
                .where(diary.user.socialId.eq(socialId), diary.date.between(startDate, endDate))
                .fetch();
    }

    @Transactional
    @Override
    public void deleteDiary(Long year, Long month, Long day, String socialId) {
        List<Diary> diariesToDelete = queryFactory
                .selectFrom(diary)
                .where(yearEq(year), monthEq(month), dayEq(day), socialIdEq(socialId))
                .fetch();

        for (Diary diaryToDelete : diariesToDelete) {
            em.remove(diaryToDelete);
        }

        em.flush();
        em.clear();
    }

    private BooleanExpression yearEq(Long year) {
        if (year == null) {
            return null;
        }
        return diary.date.year().eq(year.intValue());
    }

    private BooleanExpression monthEq(Long month) {
        if (month == null) {
            return null;
        }
        return diary.date.month().eq(month.intValue());
    }

    private BooleanExpression dayEq(Long day) {
        if (day == null) {
            return null;
        }
        return diary.date.dayOfMonth().eq(day.intValue());
    }

    private BooleanExpression socialIdEq(String socialId) {
        return isEmpty(socialId) ? null : diary.user.socialId.eq(socialId);
    }
}
