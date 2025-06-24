package com.example.demo.exception;

public class SurveySetNotFoundException extends RuntimeException {
  public SurveySetNotFoundException() {
    super("해당 문진 세트가 존재하지 않습니다.");
  }
}
