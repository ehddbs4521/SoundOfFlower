package midas.SoundOfFlower.controller;

import lombok.RequiredArgsConstructor;
import midas.SoundOfFlower.dto.request.MusicLikeRequest;
import midas.SoundOfFlower.dto.response.PageResultResponse;
import midas.SoundOfFlower.service.MusicLikesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/music")
@RestController
@RequiredArgsConstructor
public class MusicController {

    private final MusicLikesService musicLikesService;

    @GetMapping("/likes")
    public ResponseEntity<Object> getLikes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        PageResultResponse likes = musicLikesService.getLikes(socialId, page, size);

        return ResponseEntity.ok(likes);
    }

    @PostMapping("/likes")
    public ResponseEntity<Object> musicLike(@RequestBody MusicLikeRequest musicLikeRequest) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        musicLikesService.likeMusic(musicLikeRequest.getSpotify(),socialId,musicLikeRequest.isLike());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/likes")
    public ResponseEntity<Object> updateMusicLike(@RequestBody MusicLikeRequest musicLikeRequest) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String socialId = principal.getUsername();

        musicLikesService.likeMusic(musicLikeRequest.getSpotify(), socialId, musicLikeRequest.isLike());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/top-liked")
    public ResponseEntity<Object> getTopLikedspotifys(@RequestParam(defaultValue = "10") int limit) {
        Set<String> topLikedspotifys = musicLikesService.getTopLikedspotifys(limit);
        return ResponseEntity.ok(topLikedspotifys);
    }
}
