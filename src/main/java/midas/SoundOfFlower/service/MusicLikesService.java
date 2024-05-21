package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import midas.SoundOfFlower.dto.response.PageResultResponse;
import midas.SoundOfFlower.entity.Music;
import midas.SoundOfFlower.entity.MusicLike;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.redis.entity.MusicTotalLikes;
import midas.SoundOfFlower.redis.repository.MusicTotalLikesRepository;
import midas.SoundOfFlower.repository.music.MusicRepository;
import midas.SoundOfFlower.repository.musiclike.MusicLikeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static midas.SoundOfFlower.error.ErrorCode.NOT_EXIST_MUSIC_MUSICID;

@Service
@RequiredArgsConstructor
public class MusicLikesService {

    private final MusicTotalLikesRepository redisMusicTotalLikesRepository;
    private final MusicLikeRepository musicLikeRepository;
    private final MusicRepository musicRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final String TOTAL_LIKES = "totalLikes";

    @Transactional
    public void likeMusic(Long musicId, String socialId, boolean like) {

        MusicLike musicLike = musicLikeRepository.findByUser_SocialIdAndMusic_MusicId(socialId, musicId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

        Music music = musicRepository.findByMusicId(musicId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_MUSICID));

        musicLike.updateLike(like);
        musicLikeRepository.save(musicLike);

        updateMusicLike(musicId, socialId, like);

        updateMusicTotalLikes(like, music);

        musicRepository.save(music);

        MusicTotalLikes musicTotalLikes = redisMusicTotalLikesRepository.findById(musicId.toString())
                .orElse(new MusicTotalLikes(musicId.toString(), music.getTotalLikes()));

        updateRedisMusicTotalLikes(like, musicTotalLikes);
        redisMusicTotalLikesRepository.save(musicTotalLikes);

        updateSortedSet(musicId.toString(), music.getTotalLikes());
    }

    private void updateMusicLike(Long musicId, String socialId, boolean like) {
        if (like) {
            addToIndex(socialId, musicId.toString());
        } else {
            removeFromIndex(socialId, musicId.toString());
        }
    }

    public boolean isLikes(Long musicId, String socialId) {
        String indexKey = socialId;
        List<String> likedMusicIds = redisTemplate.opsForList().range(indexKey, 0, -1);

        if (likedMusicIds != null && likedMusicIds.contains(musicId.toString())) {
            return true;
        } else {

            return musicLikeRepository.findByUser_SocialIdAndMusic_MusicId(socialId, musicId)
                    .map(musicLike -> {
                        redisTemplate.opsForList().rightPush(indexKey, musicId.toString());
                        return true;
                    })
                    .orElse(false);
        }
    }


    public PageResultResponse getLikes(String socialId, int page, int size) {
        String indexKey = socialId;
        Long total = redisTemplate.opsForList().size(indexKey);

        if (total == null || total == 0) {
            List<MusicLike> musicLikes = musicLikeRepository.findByUser_SocialId(socialId);
            for (MusicLike musicLike : musicLikes) {
                redisTemplate.opsForList().leftPush(indexKey, musicLike.getMusic().getMusicId().toString());
            }
            total = redisTemplate.opsForList().size(indexKey);
        }

        int start = (page - 1) * size;
        int end = start + size - 1;

        List<String> musicIds = redisTemplate.opsForList().range(indexKey, start, end);
        boolean last = (end + 1) >= total;

        return new PageResultResponse(musicIds, last);
    }

    public Set<String> getTopLikedMusicIds(int limit) {
        Set<String> topLikedMusicIds = redisTemplate.opsForZSet().reverseRange(TOTAL_LIKES, 0, limit - 1);

        if (topLikedMusicIds == null || topLikedMusicIds.isEmpty()) {

            List<Music> topLikedMusics = musicRepository.findAll().stream()
                    .sorted(Comparator.comparingDouble(Music::getTotalLikes).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            for (Music music : topLikedMusics) {
                redisTemplate.opsForZSet().add(TOTAL_LIKES, music.getMusicId().toString(), music.getTotalLikes());
            }

            topLikedMusicIds = topLikedMusics.stream()
                    .map(music -> music.getMusicId().toString())
                    .collect(Collectors.toSet());
        }

        return topLikedMusicIds;
    }

    private static void updateRedisMusicTotalLikes(boolean like, MusicTotalLikes musicTotalLikes) {
        if (like) {
            musicTotalLikes.updateTotalLikes(musicTotalLikes.getTotalLikes() + 1);
        } else if (musicTotalLikes.getTotalLikes() > 0) {
            musicTotalLikes.updateTotalLikes(musicTotalLikes.getTotalLikes() - 1);
        }
    }

    private static void updateMusicTotalLikes(boolean like, Music music) {
        if (like) {
            music.setLikes(music.getTotalLikes() + 1);
        } else if (music.getTotalLikes() > 0) {
            music.setLikes(music.getTotalLikes() - 1);
        }
    }

    private void addToIndex(String socialId, String musicId) {
        String indexKey = socialId;
        redisTemplate.opsForList().leftPush(indexKey, musicId);
    }

    private void removeFromIndex(String socialId, String musicId) {
        String indexKey = socialId;
        redisTemplate.opsForList().remove(indexKey, 1, musicId);
    }

    private void updateSortedSet(String musicId, Double totalLikes) {
        redisTemplate.opsForZSet().add(TOTAL_LIKES, musicId, totalLikes);
    }
}
