package com.nongsan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NongSanApplication {

	public static void main(String[] args) {
		SpringApplication.run(NongSanApplication.class, args);
	}

}
