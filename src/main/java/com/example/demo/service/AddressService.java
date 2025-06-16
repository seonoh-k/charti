package com.example.demo.service;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.entity.Address;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.util.AuthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    public Address getAddressById(Long id){

        Optional<Address> byId = addressRepository.findById(id);
        if(byId.isPresent()){
            return byId.get();
        } else {
            throw new AddressNotFoundException();
        }

    }

    public Address getAddressByZipNum(AddressDTO addressDTO){

        Optional<Address> byId = addressRepository.findByZipNum(addressDTO.getZipNum());
        if(byId.isPresent()){
            return byId.get();
        } else {
            throw new AddressNotFoundException();
        }

    }

    public Address getAddressByZipNum(String zipNum){

        Optional<Address> byId = addressRepository.findByZipNum(zipNum);
        if(byId.isPresent()){
            return byId.get();
        } else {
            throw new AddressNotFoundException();
        }

    }

    public boolean existsByZipNum(String zipNum){

        boolean isExist = addressRepository.existsByZipNum(zipNum);
        return isExist;

    }

    public Optional<Address> getAddressByAllFields(AddressInfo addressInfo){

        String zipNum = addressInfo.getZipNum();
        String sido = addressInfo.getSido();
        String gugun = addressInfo.getGugun();
        String dong = addressInfo.getDong();
        String bunji = addressInfo.getBunji();
        Optional<Address> address = addressRepository.findByZipNumAndSidoAndGugunAndDongAndBunji(zipNum, sido, gugun, dong, bunji);
        return address;

    }


}
