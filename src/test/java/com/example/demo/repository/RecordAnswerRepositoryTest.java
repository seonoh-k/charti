//package com.example.demo.repository;
//
//
//
//import com.example.demo.survey.repository.RecordAnswerRepository;
//import com.example.demo.users.entity.Child;
//import com.example.demo.users.repository.ChildRepository; // 자녀를 찾기 위해 필요
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//@SpringBootTest
//class RecordAnswerRepositoryTest {
//
//    @Autowired
//    private RecordAnswerRepository recordAnswerRepository;
//
//    @Autowired
//    private ChildRepository childRepository; // Child를 가져오기 위해 주입 (없으면 다른 방법으로 Child 객체를 가져와야 합니다)
//
//    @Test
//    @Transactional
//    void debugParameterTypes() {
//        // ======================= 중요 =========================
//        // 실제 데이터가 있는 자녀(Child)의 ID를 입력해야 합니다.
//        // ======================================================
//        Long testChildId = 9L;
//
//        Child testChild = childRepository.findById(testChildId).orElse(null);
//
//        // 테스트를 진행하기 전, 해당 자녀가 DB에 존재하는지,
//        // 그리고 그 자녀가 작성한 RecordAnswer 데이터가 있는지 반드시 확인해주세요.
//        assertThat(testChild).isNotNull();
//
//        // 디버깅용 임시 메서드 호출
//        List<Object[]> results = recordAnswerRepository.findDistinctRecordDatesForDebugging(testChild);
//
//        if (results.isEmpty()) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            System.out.println("테스트 결과 없음: ID가 " + testChildId + "인 자녀의 기록문진 데이터가 DB에 있는지 확인해주세요.");
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        } else {
//            // 조회된 첫 번째 결과의 각 컬럼 타입을 출력
//            Object[] firstResult = results.get(0);
//            Object dateObject = firstResult[0];
//            Object idObject = firstResult[1];
//
//            System.out.println("==================================================");
//            System.out.println("      JPA가 반환하는 실제 파라미터 타입 확인");
//            System.out.println("--------------------------------------------------");
//            System.out.println("1. 날짜(Date) 객체의 실제 타입: " + (dateObject != null ? dateObject.getClass().getName() : "null"));
//            System.out.println("2. ID 객체의 실제 타입:       " + (idObject != null ? idObject.getClass().getName() : "null"));
//            System.out.println("==================================================");
//        }
//    }
//}