package com.example.demo.users.service;

import com.example.demo.entity.Address;
import com.example.demo.repository.AddressRepository;
import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class MemberService extends BaseService<Member, MemberRepository> {

    private final AddressRepository addressRepository;

    public MemberService(MemberRepository repository, AddressRepository addressRepository) {
        super(repository);
        this.addressRepository = addressRepository;
    }

    public void create() {

        Address address = addressRepository.findById(1L).get();

        Member newMember = new Member();
//        newMember.setName("이병건");
//        newMember.setNickname("침착맨");
        newMember.setAddress(address);
//        newMember.setPhone("0000");

        repository.save(newMember);

    }
}
