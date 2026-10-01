/*
 * 1번. BoardApplication.java - com.example.board를 기준으로 Spring Boot 애플리케이션 실행
 * 참고: java-springboot-test의 @SpringBootApplication과 SpringApplication.run 작성 방식.
 */
package com.example.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardApplication.class, args);
	}

}
