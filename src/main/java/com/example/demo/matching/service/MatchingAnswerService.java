//package com.example.demo.matching.service;
//
//import com.example.demo.fcm.service.FcmService;
//import com.example.demo.matching.entity.MatchingAnswer;
//import com.example.demo.matching.repository.MatchingAnswerRepository;
//import com.example.demo.matching.repository.MatchingRepository;
//import com.example.demo.users.repository.ExpertRepository;
//import com.example.demo.users.entity.Expert;
//import com.example.demo.matching.entity.Matching;
//import com.example.demo.enums.MatchingStatus;
//import com.example.demo.enums.FcmCategory;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class MatchingAnswerService {
//
//    private final MatchingAnswerRepository ansRepo;
//    private final MatchingRepository matchRepo;
//    private final ExpertRepository expertRepo;
//    private final FcmService fcmService;
//
//    /**
//     * 전문가 답변 저장 → 상태 변경 → 부모님께 알림
//     */
//    public void save(MatchingAnswer form) {
//        // 1) 저장
//        ansRepo.save(form);
//
//        // 2) 상태 변경 (RESPONDED)
//        Matching m = form.getMatching();
//        m.setStatus(MatchingStatus.RESPONDED);
//        matchRepo.save(m);
//
//        // 3) 부모님께 FCM 알림
//        Expert expert = form.getExpert();
//        String title = "[답변 도착] " + m.getTitle();
//        String body  = "전문가 " + expert.getUsers().getName() + " 님이 답변을 남겼습니다.";
//        String url   = "/matching/detail/" + m.getId();
//        fcmService.sendNotificationToUser(
//                m.getChild().getParent().getUsers(),
//                title,
//                body,
//                FcmCategory.SPECIAL,
//                url
//        );
//    }
//
//    public List<MatchingAnswer> findByMatchingId(Long matchingId) {
//        return ansRepo.findByMatchingId(matchingId);
//    }
//}
package com.example.demo.matching.service;

import com.example.demo.fcm.service.FcmService;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.repository.MatchingAnswerRepository;
import com.example.demo.matching.repository.MatchingRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.entity.Expert;
import com.example.demo.matching.entity.Matching;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.enums.FcmCategory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchingAnswerService {

    private final MatchingAnswerRepository ansRepo;
    private final MatchingRepository matchRepo;
    private final ExpertRepository expertRepo;
    private final FcmService fcmService;

    /**
     * 전문가 답변 저장 → 상태 변경 → 부모님께 알림
     */
    public void save(MatchingAnswer form) {
        // 1) 저장
        ansRepo.save(form);

        // 2) 상태 변경 (RESPONDED)
        Matching m = form.getMatching();
        m.setStatus(MatchingStatus.RESPONDED);
        matchRepo.save(m);

        // 3) 부모님께 FCM 알림
        Expert expert = form.getExpert();
        Users expertUser = expert.getUsers(); // 보내는 사람 (전문가)
        Users parentUser = m.getChild().getParent().getUsers(); // 받는 사람 (학부모)

        String title = "[답변 도착] " + m.getTitle();
        String body  = "전문가 " + expertUser.getName() + " 님이 답변을 남겼습니다.";
        String url   = "/matching/detail/" + m.getId();

        fcmService.sendNotification(
                expertUser,
                parentUser,
                title,
                body,
                FcmCategory.SPECIAL,
                url
        );
    }

    public List<MatchingAnswer> findByMatchingId(Long matchingId) {
        return ansRepo.findByMatchingId(matchingId);
    }
}
