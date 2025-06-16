package com.example.demo.repository;

import com.example.demo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByZipNum(String zipNum);
    boolean existsByZipNum(String zipNum);
    Optional<Address> findByZipNumAndSidoAndGugunAndDongAndBunji(
            String zipNum,
            String sido,
            String gugun,
            String dong,
            String bunji
    );
}
