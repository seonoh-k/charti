package com.example.demo.service;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ExpertRepository expertRepository;
    private final ManagerRepository managerRepository;
    private final GroupRepository groupRepository;


    public AddressDTO entityToDto(Address address) {

        if (address == null) return null;

        return  AddressDTO.builder()
                .id(address.getId())
                .zipNum(address.getZipNum())
                .sido(address.getSido())
                .gugun(address.getGugun())
                .dong(address.getDong())
                .bunji(address.getBunji())
                .build();

    }
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

    public AddressDTO getAddressByUid(String uid) {
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new RuntimeException("회원 정보 없음"));

        // 1) Member 타입
        Optional<Member> memberOpt = memberRepository.findById(user.getId());
        if (memberOpt.isPresent() && memberOpt.get().getAddress() != null) {
            return entityToDto(addressRepository.findById(memberOpt.get().getAddress().getId())
                    .orElse(null)); // 주소가 없으면 null 반환
        }

        // 2) Expert 타입
        Optional<Expert> expertOpt = expertRepository.findById(user.getId());
        if (expertOpt.isPresent() && expertOpt.get().getAddress() != null) {
            return entityToDto(addressRepository.findById(expertOpt.get().getAddress().getId())
                    .orElse(null));
        }

        // 3) Manager 타입 → Group → Address
        Optional<Manager> managerOpt = managerRepository.findById(user.getId());
        if (managerOpt.isPresent() && managerOpt.get().getGroup() != null) {
            Group group = groupRepository.findById(managerOpt.get().getGroup().getId())
                    .orElse(null);
            if (group != null && group.getAddress() != null) {
                return entityToDto(addressRepository.findById(group.getAddress().getId())
                        .orElse(null));
            }
        }

        // 모두 해당없으면 주소정보 없음
        return null;
    }



}
