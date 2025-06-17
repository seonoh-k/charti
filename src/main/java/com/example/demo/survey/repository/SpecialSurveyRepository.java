//package com.example.demo.survey.repository;
//
//import com.example.demo.survey.entity.GroupSurvey;
//import com.example.demo.survey.entity.SpecialSurvey;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Arrays;
//import java.util.List;
//
//public interface SpecialSurveyRepository extends JpaRepository<SpecialSurvey, Long> {
//
//    //    List<SpecialSurvey> findByChildId(Long childId);
//    @Query("""
//        select ss
//          from SpecialSurvey ss
//          join ss.surveySets sset
//         where sset.setId = :setId
//        """)
//    List<SpecialSurvey> findBySurveySetId(@Param("setId") Long setId);
//}
