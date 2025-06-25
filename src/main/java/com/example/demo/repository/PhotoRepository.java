package com.example.demo.repository;

import com.example.demo.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByAlbumId(Long AlbumId, Pageable pageable);

    List<Photo> findByMatchingId(Long matchingId);
}
