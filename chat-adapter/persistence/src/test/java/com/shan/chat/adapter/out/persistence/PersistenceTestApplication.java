package com.shan.chat.adapter.out.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * persistence 모듈 통합 테스트용 최소 부트스트랩.
 * persistence 패키지만 스캔하여 JPA + Querydsl 컨텍스트를 구성한다.
 */
@SpringBootApplication(scanBasePackages = "com.shan.chat.adapter.out.persistence")
public class PersistenceTestApplication {
}

