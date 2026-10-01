/*
 * 2번. TimeConfig.java - 서울 시간 Clock Bean 및 JPA Auditing 설정
 * 참고: 아래 수업 예제의 Clock/DateTimeProvider/Optional 방식을 그대로 사용한다.
 * https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/main/java/com/example/board/config/TimeConfig.java
 */
package com.example.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Configuration
// [수업 예제 외 추가 / 원본 보완] Bean 이름을 명시해 Auditing이 서울 Clock의 시간 공급자를 사용하게 한다.
@EnableJpaAuditing(dateTimeProviderRef = "seoulDateTimeProvider")
public class TimeConfig {

    @Bean
    public Clock clock() {
        // 2번: 필요한 클래스에서 Clock을 생성자 주입받아 같은 시간 기준을 사용한다.
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public DateTimeProvider seoulDateTimeProvider(Clock clock) {
        // 2번/3번: 저장/변경 시점의 ZonedDateTime을 Auditing에 공급한다.
        return () -> Optional.of(ZonedDateTime.now(clock));
    }
}
