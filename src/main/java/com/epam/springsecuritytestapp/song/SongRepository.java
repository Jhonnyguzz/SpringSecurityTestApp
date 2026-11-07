package com.epam.springsecuritytestapp.song;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findTop5ByOrderByCreatedAtDesc();
}
