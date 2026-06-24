package com.rishanth.deskmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeskmindApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeskmindApplication.class, args);
	}

}
