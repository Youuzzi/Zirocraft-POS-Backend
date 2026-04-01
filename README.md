# Zirocraft POS - Backend API ☕🛡️

Repository ini berisi logika bisnis, manajemen database, dan sistem keamanan transaksi untuk **Zirocraft POS**. Dibangun menggunakan framework Spring Boot dengan fokus pada integritas data finansial dan penanganan transaksi tinggi.

### 🔗 Related Repository
* **Frontend Interface:** [Zirocraft-POS-Frontend](https://github.com/Youuzzi/Zirocraft-POS-Frontend)

### 🛠️ Tech Stack
* **Java 17** & **Spring Boot 3.x**
* **Spring Security** (Stateless JWT Authentication)
* **Spring Data JPA** (Hibernate)
* **MySQL Database**
* **Maven** (Dependency Manager)

### ✨ Fitur Utama (v1.0 Stable)
* **High Concurrency Control:** Implementasi *Pessimistic Write Locking* untuk mencegah kebocoran stok saat transaksi bersamaan.
* **Anti-Double Billing:** Sistem *Idempotency Key* (UUID) untuk mencegah transaksi ganda akibat gangguan jaringan.
* **Financial Integrity:** Logika pembulatan otomatis ke 500 terdekat (Standard UMKM) menggunakan `BigDecimal` yang presisi.
* **Strict Shift Management:** Sistem rekonsiliasi kasir (*Blind Closing*) untuk audit keuangan yang akurat.
* **XSS Protection:** Pembersihan input kategori dan produk menggunakan *OWASP HTML Sanitizer*.
* **Global Exception Handling:** Standarisasi respon error API dalam format JSON yang rapi.

### 🚀 Cara Menjalankan Project
1. **Clone** repository ini:
   ```bash
   git clone [https://github.com/Youuzzi/Zirocraft-POS-Backend.git](https://github.com/Youuzzi/Zirocraft-POS-Backend.git)
Database: Buat database baru di MySQL Workbench (contoh: billing_app).

Migration: Execute perintah SQL ini di MySQL agar fitur keamanan transaksi aktif:

SQL
ALTER TABLE tbl_orders ADD COLUMN idempotency_key VARCHAR(255) UNIQUE;
Configuration: Sesuaikan kredensial database kamu di file src/main/resources/application.properties.

Run: Jalankan project melalui IntelliJ IDEA atau gunakan perintah berikut di terminal:

Bash
./mvnw spring-boot:run
Developed by Yozi Heru Maulana | Zirocraft Studio
