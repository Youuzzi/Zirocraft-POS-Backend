# Zirocraft POS - Backend API ☕🛡️

Repository ini berisi logika bisnis, manajemen database, dan sistem keamanan transaksi untuk **Zirocraft POS**. Dibangun menggunakan framework Spring Boot dengan standar industri untuk integritas data finansial dan penanganan transaksi tinggi.

### 🔗 Related Repository
* **Frontend Interface:** [Zirocraft-POS-Frontend](https://github.com/Youuzzi/Zirocraft-POS-Frontend)

---

### 🛠️ Tech Stack
* **Java 21 (LTS) & Spring Boot 3.5.x**
* **Spring Security** (Stateless JWT Authentication)
* **Spring Data JPA** (Hibernate)
* **MySQL (Production Database) & H2 (Testing Database)**
* **Maven** (Dependency Manager)

---

### ✨ Fitur Utama (Industrial Grade)
* **High Concurrency Control:** Implementasi *Pessimistic Write Locking* untuk mencegah kebocoran stok saat transaksi tinggi secara bersamaan.
* **Anti-Double Billing:** Sistem *Idempotency Key* (UUID) untuk menjamin satu transaksi hanya diproses satu kali, mencegah duplikasi data akibat gangguan jaringan.
* **Financial Integrity:** Logika pembulatan otomatis ke kelipatan 500 terdekat menggunakan `BigDecimal` yang presisi.
* **Strict Shift Management:** Sistem rekonsiliasi kasir (*Blind Closing*) untuk audit keuangan yang akurat.
* **XSS Protection:** Pembersihan input kategori dan produk menggunakan *OWASP HTML Sanitizer*.
* **Global Exception Handling:** Standarisasi respon error API dalam format JSON yang rapi.

---

### 🧪 Automated Testing & Quality Audit
Proyek ini mengimplementasikan strategi pengujian otomatis untuk menjaga stabilitas sistem tanpa konfigurasi manual yang rumit:
* **Isolated Integration Test:** Menggunakan **H2 In-Memory Database** (MySQL Compatibility Mode). Pengujian logika database berjalan di RAM, sehingga tidak mengotori database produksi.
* **Zero Configuration Testing:** Cukup jalankan perintah test, sistem akan mengonfigurasi environment secara otomatis melalui Spring Profiles (`test`).

---

### 🚀 Cara Menjalankan Project

#### 1. Clone Repository
```bash
git clone https://github.com/Youuzzi/Zirocraft-POS-Backend.git
cd Zirocraft-POS-Backend
2. Verify & Audit (Recommended)
Jalankan pengujian otomatis untuk memastikan semua logika bisnis dan skema database berjalan sempurna:

Bash
./mvnw test
3. Database Configuration
Buat database MySQL: billing_app.

Jalankan SQL Migration jika diperlukan:

SQL
ALTER TABLE tbl_orders ADD COLUMN idempotency_key VARCHAR(255) UNIQUE;
Sesuaikan kredensial database di file src/main/resources/application.properties.

4. Run Application
Bash
./mvnw spring-boot:run
✍️ Author
Developed by Yozi Heru Maulana | Zirocraft Studio 🚀