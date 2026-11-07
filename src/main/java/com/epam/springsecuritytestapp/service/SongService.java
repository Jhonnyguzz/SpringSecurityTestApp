package com.epam.springsecuritytestapp.service;

import com.epam.springsecuritytestapp.domain.Song;
import com.epam.springsecuritytestapp.dto.SongForm;
import com.epam.springsecuritytestapp.repository.SongRepository;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
        return songRepository.save(song);
    }
}
