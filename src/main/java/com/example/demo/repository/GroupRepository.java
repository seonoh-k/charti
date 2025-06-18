package com.example.demo.repository;

import com.example.demo.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long> {

    List<Group> findByGroupNameContainingIgnoreCase(String name);
}
