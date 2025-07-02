package com.example.demo.fcm.service;

import com.example.demo.fcm.dto.FcmHistorySearchDto;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.enums.FcmCategory;
import com.example.demo.users.entity.Users;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmHistoryService {

    private final FcmSendHistoryRepository historyRepository;

    public Page<FcmSendHistory> findHistory(FcmHistorySearchDto searchDto, Pageable pageable) {

        Specification<FcmSendHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDto.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sentAt"), searchDto.getStartDate().atStartOfDay()));
            }
            if (searchDto.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sentAt"), searchDto.getEndDate().plusDays(1).atStartOfDay()));
            }
            if (searchDto.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), searchDto.getCategory()));
            }
            if (StringUtils.hasText(searchDto.getSenderName())) {
                String keyword = searchDto.getSenderName().trim();
                // '시스템'으로 검색할 경우, sender가 null인 이력을 찾습니다.
                if ("시스템".equals(keyword)) {
                    predicates.add(cb.isNull(root.get("sender")));
                }
                // 그 외의 경우, sender의 이름으로 검색합니다.
                else {
                    Join<FcmSendHistory, Users> senderJoin = root.join("sender", JoinType.INNER);
                    predicates.add(cb.like(senderJoin.get("name"), "%" + keyword + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return historyRepository.findAll(spec, pageable);
    }

    public List<FcmSendHistory> getRecentGroupNotices(String senderName, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt"));
        return historyRepository.findGroupNoticesBySenderName(senderName, pageable);
    }
}