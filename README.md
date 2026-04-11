# ZiroShop POS – Backend API ☕🛡️

> REST API untuk sistem kasir ZiroShop. Mengelola logika bisnis, manajemen database, shift kasir, dan keamanan transaksi. Dibangun menggunakan Spring Boot dengan standar industri.

---

## 🔗 Related Repository
- **Frontend:** [Zirocraft-POS-Frontend](https://github.com/Youuzzi/Zirocraft-POS-Frontend)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Build Tool | Maven |
| API | RESTful API |

---

## ✨ Fitur Utama

### 🔐 Authentication & Authorization
- **JWT Authentication** — Stateless token-based authentication
- **Role-Based Access Control** — Role `ADMIN` dan `USER` dengan akses endpoint berbeda
- **User Management** — CRUD user dikelola oleh Admin

### 🧾 Shift Management
- **Open Shift** — Buka shift dengan input modal awal (uang di laci)
- **Expense Tracking** — Catat pengeluaran operasional selama shift (beli es batu, bahan, dll.)
- **Close Shift** — Tutup shift dengan rekonsiliasi uang aktual vs expected
- **Shift History** — Riwayat seluruh shift tersedia untuk Admin
- **Auto Cleanup** — Scheduled task untuk membersihkan shift yang tidak terselesaikan

### 🛒 Transaksi & Order
- **Checkout** — Proses transaksi terintegrasi dengan shift aktif
- **Idempotency Key** — Mencegah duplikasi order akibat gangguan jaringan atau double submit
- **Nomor Antrian Otomatis** — Setiap order mendapat nomor antrian harian yang di-generate otomatis
- **Validasi Stok Real-time** — Stok dicek dan dikurangi otomatis saat checkout
- **Void Order** — Admin dapat membatalkan order dengan alasan void, tercatat siapa yang melakukan void
- **Search Order** — Pencarian order berdasarkan query
- **Recent Orders** — Endpoint untuk mengambil order terbaru per shift

### 📦 Manajemen Produk & Kategori
- CRUD Item — Tambah, edit, hapus produk dengan upload foto
- CRUD Kategori — Kelola kategori produk
- **Smart Stock Alert** — Stok < 5 unit ditandai sebagai "running low"
- **Restock via Edit** — Update stok produk melalui fitur edit item

### ⚙️ Store Settings
- **Nama Toko** — Konfigurasi nama toko yang tampil di struk
- **Modal Laci** — Atur jumlah uang awal di laci kasir

### 🛡️ Security & Reliability
- **Pessimistic Write Locking** — Mencegah race condition stok saat transaksi bersamaan
- **Global Exception Handler** — Standarisasi format error response dalam JSON
- **XSS-safe Input** — Validasi input di level request

---

## 📁 Struktur Project

```
src/main/java/
├── config/          # Security, Web config
├── controller/      # Auth, Category, Item, Order, Setting, Shift, User
├── entity/          # JPA Entities
├── filter/          # JWT Request Filter
├── repository/      # Spring Data JPA Repositories
├── request/         # Request DTOs
├── response/        # Response DTOs
├── service/         # Business Logic (Interface + Impl)
├── task/            # Scheduled Tasks (ShiftCleanup)
└── util/            # JWT Utility
```

---

## 🚀 Cara Menjalankan

### Prerequisites
- Java 21
- MySQL 8.x
- Maven

### Setup

```bash
# Clone repository
git clone https://github.com/Youuzzi/Zirocraft-POS-Backend.git
cd Zirocraft-POS-Backend

# Buat database MySQL
CREATE DATABASE zirocraft_pos;

# Konfigurasi application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/zirocraft_pos
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Jalankan
mvn spring-boot:run
```

---

## 👨‍💻 Developer

**Yozi Heru Maulana** — [github.com/Youuzzi](https://github.com/Youuzzi)

*Zirocraft Studio*