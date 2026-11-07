package com.epam.springsecuritytestapp.repository;

import com.epam.springsecuritytestapp.domain.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

// Not exposed by Spring Data REST: songs are created only through SongController
@RepositoryRestResource(exported = false)
public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findTop5ByOrderByCreatedAtDesc();

    List<Song> findAllByOrderByCreatedAtDesc();
}
