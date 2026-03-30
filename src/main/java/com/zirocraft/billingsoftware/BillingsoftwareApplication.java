package com.zirocraft.billingsoftware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillingsoftwareApplication {

	public static void main(String[] args) {
		System.out.println(">>> Zirocraft POS Engine: Starting up...");
		SpringApplication.run(BillingsoftwareApplication.class, args);
		System.out.println(">>> Zirocraft POS Engine: Ready to use! ✅");
	}
}