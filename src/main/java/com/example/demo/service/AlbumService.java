package com.example.demo.service;

import com.example.demo.entity.Album;
import com.example.demo.repository.AlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AlbumService extends BaseService<Album, AlbumRepository> {

    public AlbumService(AlbumRepository repository) {
        super(repository);
    }

    public Page<Album> getPagedList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.asc("id")));

        return this.repository.findByMember_Id(userId, pageable);
    }
}
