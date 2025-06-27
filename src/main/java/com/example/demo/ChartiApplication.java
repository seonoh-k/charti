package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // BaseEntity를 적용하기 위한 어노테이션
@EnableScheduling // 스케줄러 적용하기 위한 어노테이션
@EnableAsync
public class ChartiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChartiApplication.class, args);
	}

}
