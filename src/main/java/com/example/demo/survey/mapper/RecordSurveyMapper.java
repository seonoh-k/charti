//package com.example.demo.survey.mapper;
//
//import com.example.demo.survey.dto.RecordSurveyRequest;
//import com.example.demo.survey.dto.RecordSurveyResponse;
//import com.example.demo.survey.entity.RecordSurvey;
//
//public class RecordSurveyMapper {
//
//    // RecordSurveyRequest → RecordSurvey (Entity 변환)
//    public static RecordSurvey toEntity(RecordSurveyRequest request) {
//        RecordSurvey entity = new RecordSurvey();
//        entity.setAgeGroup(request.getAgeGroup());
//        entity.setQuestion(request.getQuestion());
//        entity.setAnswer(""); // 기본값
//        return entity;
//    }
//
//    // RecordSurvey → RecordSurveyResponse (DTO 변환)
//    public static RecordSurveyResponse toResponse(RecordSurvey entity) {
//        return new RecordSurveyResponse(
//                entity.getSurveyId(),
//                entity.getQuestion(),
//                entity.getAgeGroup(),
//                entity.getCreatedAt()
//        );
//    }
//}
