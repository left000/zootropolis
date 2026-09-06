package com.zootropolis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZootropolisApplication {

	public static void main(String[] args) {
		System.out.println("Ciao!");

		SpringApplication.run(ZootropolisApplication.class, args);
	}

}
