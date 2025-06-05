package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class BaseEntity {

    // 대부분의 엔티티에서 상태, 생성일, 삭제일을 저장하기 때문에 모듈화
    // 이 추상 클래스를 상속 받음으로써 상태, 생성일, 삭제일은 자동으로 작성됨

    // 생성일
    @CreatedDate
    private LocalDateTime createdAt;

    // 삭제일
    private LocalDateTime deletedAt;

    // 활성화 상태 - true = 삭제됨 / false = 정상
    @Column(nullable = false)
    private boolean deleted = false;

    // 삭제된 정보인지 확인
    public Boolean isDeleted() {
        return deleted;
    }

    // soft delete. 삭제일에 해당 일자를 저장하고 활성화 상태를 true로 변경
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.deleted = true;
    }

    // 정보 복구. 삭제일 컬럼에 저장된 정보를 삭제하고 활성화 상태를 false로 변경
    public void restore() {
        this.deletedAt = null;
        this.deleted = false;
    }
}
