package com.example.demo.service;

import com.example.demo.entity.Album;
import com.example.demo.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class AlbumService extends BaseService<Album, AlbumRepository> {

    public AlbumService(AlbumRepository repository) {
        super(repository);
    }
}
