package com.example.demo.service;

import com.example.demo.entity.Photo;
import com.example.demo.repository.PhotoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PhotoService extends BaseService<Photo, PhotoRepository> {

    public PhotoService(PhotoRepository repository) {
        super(repository);
    }

    public Page<Photo> getPagedList(Long albumId, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.asc("id")));

        return this.repository.findByAlbumId(albumId, pageable);
    }
}
