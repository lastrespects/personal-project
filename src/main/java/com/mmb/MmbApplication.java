package com.mmb; // com.mmb 바로 아래에 있어야 합니다.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 👈 스프링 부트의 모든 자동 설정을 활성화하는 핵심 어노테이션입니다.
public class MmbApplication {

    public static void main(String[] args) {
        SpringApplication.run(MmbApplication.class, args);
    }
}