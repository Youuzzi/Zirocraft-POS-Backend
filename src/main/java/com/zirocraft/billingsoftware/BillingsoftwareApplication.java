package com.zirocraft.billingsoftware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillingsoftwareApplication {

	public static void main(String[] args) {
		// --- TAMBAHKAN KODE INI ---
		System.out.println(">>> POSISI KAKI SERVER (User Dir): " + System.getProperty("user.dir"));
		// --------------------------

		SpringApplication.run(BillingsoftwareApplication.class, args);
	}

}	