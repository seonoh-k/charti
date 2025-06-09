package com.example.demo.service;

import com.example.demo.entity.Child;
import com.example.demo.repository.ChildRepository;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChildService extends BaseService<Child, Long, ChildRepository> {
//        extends BaseService<Child, ChildRepository> {

    // 리포지토리 생성자
    public ChildService(ChildRepository repository) {
        super(repository);
    }

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
        return getRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다. ID=" + id));
    }

    public Child get(Long childId) {

        return null;}
}
