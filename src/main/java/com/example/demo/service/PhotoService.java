package com.example.demo.service;

import com.example.demo.entity.Photo;
import com.example.demo.repository.PhotoRepository;
import org.springframework.stereotype.Service;

@Service
public class PhotoService extends BaseService<Photo, PhotoRepository> {

    public PhotoService(PhotoRepository repository) {
        super(repository);
    }
}
