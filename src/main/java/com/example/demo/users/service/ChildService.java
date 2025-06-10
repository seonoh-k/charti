package com.example.demo.users.service;

import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.repository.ChildRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChildService extends BaseService<Child, ChildRepository> {

    public ChildService(ChildRepository repository) {
        super(repository);
    }
//        extends BaseService<Child, ChildRepository> {

    // 수정 메소드 작성 예시
    public void updateChild(Integer id, String name, String nickname) {
        // 부모 클래스의 메소드로 데이터 조회
//        Child child = super.get(id);
//        child.setName(name);
//        child.setNickname(nickname);
        // 부모 클래스의 메소드로 데이터 수정
//        super.update(child);
    }

    public Child findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다. ID=" + id));
    }

    public Child get(Long childId) {

        return null;}
}
