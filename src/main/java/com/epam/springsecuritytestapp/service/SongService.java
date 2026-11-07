package com.epam.springsecuritytestapp.service;

import com.epam.springsecuritytestapp.domain.Song;
import com.epam.springsecuritytestapp.dto.SongForm;
import com.epam.springsecuritytestapp.repository.SongRepository;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional(readOnly = true)
    public List<Song> findAllNewestFirst() {
        return songRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Song addSong(SongForm form, AppUserPrincipal user) {
        Song song = new Song();
        song.setTitle(form.getTitle().trim());
        song.setArtist(form.getArtist().trim());
        // Taken from the authenticated principal, never from the request
        song.setAddedBy(user.getEmail());
        song.setCreatedAt(Instant.now());
        Song saved = songRepository.save(song);
        log.info("Song added id={} title='{}' artist='{}' by={}", saved.getId(), saved.getTitle(), saved.getArtist(), saved.getAddedBy());
        return saved;
    }
}
