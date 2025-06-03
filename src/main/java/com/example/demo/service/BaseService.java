package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 서비스 클래스에서 상속 받아 사용할 추상 클래스
// T : 이 추상 클래스를 상속 받은 자식 클래스에서 사용할 엔티티
// R : 이 추상 클래스를 상속 받은 자식 클래스에서 사용할 리포지토리
// 사용 방법 : public class 클래스명 extends MainService<엔티티 타입, 리포지토리명>
@RequiredArgsConstructor
public abstract class BaseService<T, R extends JpaRepository<T, Integer>> {

    // 이 추상 클래스로 인해 기본적인 CRUD는 각 서비스 클래스에 작성하지 않아도 됨.

    private final R repository;

    // 단일 조회
    public T get(Integer id) {
        return repository.findById(id).get();
    }

    // 전체 조회
    public List<T> getList() {
        return repository.findAll();
    }

    // 생성
    public void create(T t) {
        repository.save(t);
    }

    // 수정
    public void update(T t) {
        repository.save(t);
    }

    // 삭제
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
