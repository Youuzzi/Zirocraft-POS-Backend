package com.zirocraft.billingsoftware.entity;

import jakarta.persistence.*;  // Import ini buat @Entity, @Id, dll
import lombok.AllArgsConstructor; // Import Lombok
import lombok.Builder;            // Import Lombok
import lombok.Data;               // Import Lombok
import lombok.NoArgsConstructor;  // Import Lombok
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

// --- BAGIAN INI YANG TADI KAMU KURANG ---
@Entity
@Table(name = "tbl_category") // Nama tabel di database nanti
@Data                 // Otomatis bikin Getter & Setter
@Builder              // <--- INI OBATNYA supaya .builder() jalan!
@NoArgsConstructor    // Wajib ada
@AllArgsConstructor   // Wajib ada
// ----------------------------------------

public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String categoryId;

    @Column(unique = true)
    private String name;

    private String description;
    private String bgColor;
    private String imgUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}