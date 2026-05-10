package com.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SmartCampusApplication implements CommandLineRunner {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SmartCampusApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		String password = "1234";
		String encodedPassword = passwordEncoder.encode(password);

		System.out.println("Encoded Password: " + encodedPassword);
	
	}

}
