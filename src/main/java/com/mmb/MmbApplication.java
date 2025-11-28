package com.mmb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan(basePackages = "com.mmb.dao") 
@SpringBootApplication
@EnableAsync
public class MmbApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringApplication.class, args);
	}
}