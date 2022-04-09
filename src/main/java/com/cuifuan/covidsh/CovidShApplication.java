package com.cuifuan.covidsh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.cuifuan.covidsh.mapper")
public class CovidShApplication {

	public static void main(String[] args) {
		SpringApplication.run(CovidShApplication.class, args);
	}

}
