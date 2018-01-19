package com.pp.lot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PplotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PplotApplication.class, args);
	}
}
