package com.zirocraft.billingsoftware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Manggil settingan H2 dari application-test.properties
class BillingsoftwareApplicationTests {

	@Test
	void contextLoads() {
		// Test ini memastikan jantung aplikasi ZiroPOS bisa berdetak normal
		// tanpa perlu koneksi ke database MySQL asli.
		System.out.println(">>> Zirocraft POS Audit: System Engine is Stable! ✅");
	}

}