package com.example.demo.repository;

import com.example.demo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT a FROM Address a WHERE a.dong LIKE %:dong%")
    List<Address> findSimpleByDong(@Param("dong") String dong);
}
