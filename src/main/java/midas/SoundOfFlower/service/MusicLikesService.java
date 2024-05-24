package midas.SoundOfFlower.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.dto.response.PageResultResponse;
import midas.SoundOfFlower.entity.Music;
import midas.SoundOfFlower.entity.MusicLike;
import midas.SoundOfFlower.entity.User;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.redis.entity.MusicTotalLikes;
import midas.SoundOfFlower.redis.repository.MusicTotalLikesRepository;
import midas.SoundOfFlower.repository.music.MusicRepository;
import midas.SoundOfFlower.repository.musiclike.MusicLikeRepository;
import midas.SoundOfFlower.repository.user.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static midas.SoundOfFlower.error.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicLikesService {

    private final UserRepository userRepository;
    private final MusicTotalLikesRepository redisMusicTotalLikesRepository;
    private final MusicLikeRepository musicLikeRepository;
    private final MusicRepository musicRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final String TOTAL_LIKES = "totalLikes";

    @Transactional
    public void likeMusic(String spotify, String socialId, boolean like) {
        MusicLike musicLike=null;
        if (like) {
            musicLike = musicLikeRepository.findByUser_SocialIdAndMusic_spotify(socialId, spotify)
                    .orElseGet(() -> {
                        Music music = musicRepository.findBySpotify(spotify)
                                .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_SPOTIFY));
                        User user = userRepository.findBySocialId(socialId)
                                .orElseThrow(() -> new CustomException(NOT_EXIST_USER_SOCIALID));
                        MusicLike newMusicLike = MusicLike.builder()
                                .music(music)
                                .user(user)
                                .isLike(true)
                                .build();
                        return newMusicLike;
                    });
            musicLike.updateLike(true);

            musicLikeRepository.save(musicLike);
        } else {
            musicLike = musicLikeRepository.findByUser_SocialIdAndMusic_spotify(socialId, spotify)
                    .orElseThrow(() -> new CustomException(NOT_EXIST_MUSIC_SPOTIFY));
            musicLike.updateLike(false);
            musicLikeRepository.save(musicLike);
        }


        updateMusicLike(spotify, socialId, like);

        updateMusicTotalLikes(like, musicLike.getMusic());

        musicRepository.save(musicLike.getMusic());

        MusicTotalLikes musicTotalLikes = redisMusicTotalLikesRepository.findById(spotify)
                .orElse(new MusicTotalLikes(spotify, musicLike.getMusic().getTotalLikes()));

        updateRedisMusicTotalLikes(like, musicTotalLikes);
        redisMusicTotalLikesRepository.save(musicTotalLikes);

        updateSortedSet(spotify.toString(), musicLike.getMusic().getTotalLikes());
    }

    private void updateMusicLike(String spotify, String socialId, boolean like) {
        if (like) {
            addToIndex(socialId, spotify);
        } else {
            removeFromIndex(socialId, spotify);
        }
    }

    public boolean isLikes(String spotify, String socialId) {
        String indexKey = socialId;
        List<String> likedspotifys = redisTemplate.opsForList().range(indexKey, 0, -1);

        if (likedspotifys != null && likedspotifys.contains(spotify)) {
            return true;
        } else {

            return musicLikeRepository.findByUser_SocialIdAndMusic_spotify(socialId, spotify)
                    .map(musicLike -> {
                        redisTemplate.opsForList().rightPush(indexKey, spotify.toString());
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
                redisTemplate.opsForList().leftPush(indexKey, musicLike.getMusic().getSpotify().toString());
            }
            total = redisTemplate.opsForList().size(indexKey);
        }

        int start = (page - 1) * size;
        int end = start + size - 1;

        List<String> spotifys = redisTemplate.opsForList().range(indexKey, start, end);
        boolean last = (end + 1) >= total;

        return new PageResultResponse(spotifys, last);
    }

    public Set<String> getTopLikedspotifys(int limit) {
        Set<String> topLikedspotifys = redisTemplate.opsForZSet().reverseRange(TOTAL_LIKES, 0, limit - 1);

        if (topLikedspotifys == null || topLikedspotifys.isEmpty()) {

            List<Music> topLikedMusics = musicRepository.findAll().stream()
                    .sorted(Comparator.comparingDouble(Music::getTotalLikes).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            for (Music music : topLikedMusics) {
                redisTemplate.opsForZSet().add(TOTAL_LIKES, music.getSpotify().toString(), music.getTotalLikes());
            }

            topLikedspotifys = topLikedMusics.stream()
                    .map(music -> music.getSpotify().toString())
                    .collect(Collectors.toSet());
        }

        return topLikedspotifys;
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
            music.setTotalLikes(music.getTotalLikes() + 1);
        } else if (music.getTotalLikes() > 0) {
            music.setTotalLikes(music.getTotalLikes() - 1);
        }
    }

    private void addToIndex(String socialId, String spotify) {
        String indexKey = socialId;
        redisTemplate.opsForList().leftPush(indexKey, spotify);
    }

    private void removeFromIndex(String socialId, String spotify) {
        String indexKey = socialId;
        redisTemplate.opsForList().remove(indexKey, 1, spotify);
    }

    private void updateSortedSet(String spotify, Double totalLikes) {
        redisTemplate.opsForZSet().add(TOTAL_LIKES, spotify, totalLikes);
    }
}
