package com.example.demo.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Firebase Config
 * FireBase와 연동하여 Firebase SDK가 제공하는 라이브러리를 사용
 */
@Configuration
@Log4j2
public class FirebaseConfig {

    /**
     * JSON 형식의 서비스 계정 키 파일에 정의된 설정을 토대로 스트림 형태로 받아와 초기화한다.
     *
     * @return
     * @throws IOException
     */
    @Bean
    public FirebaseApp initializeFirebase() throws IOException {

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("charti-acKey.json").getInputStream()))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            return firebaseApp;
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp){
        return FirebaseAuth.getInstance(firebaseApp);
    }

    /**
     * FirebaseMessaging Bean 등록
     * FirebaseApp이 이미 초기화된 상태이므로, 해당 인스턴스를 기반으로 Messaging 인스턴스를 반환한다.
     *
     * @param firebaseApp 초기화된 FirebaseApp 인스턴스
     * @return FirebaseMessaging 인스턴스
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}