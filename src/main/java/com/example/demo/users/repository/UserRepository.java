package com.example.demo.users.repository;


import com.example.demo.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,Long> {

    public Optional<Users> findByUsername(String username);
    public Optional<Users> findByUuid(String uuid);
    public boolean existsByUuid(String uuid);
    public boolean existsByUsername(String email);
    // boolean existsByEmailAndName(String email, String name);

    boolean existsByPhoneNumber(String phone);
    public Optional<Users> findByProviderAndProviderId(String provider, String providerId);

}
