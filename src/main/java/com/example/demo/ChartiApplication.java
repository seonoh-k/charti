package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // BaseEntity를 적용하기 위한 어노테이션
public class ChartiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChartiApplication.class, args);
	}

}
