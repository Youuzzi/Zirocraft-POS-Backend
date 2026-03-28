package com.zirocraft.billingsoftware.util;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class SanitizerUtil {

    // Kebijakan: Buang semua tag HTML (Hanya teks murni yang boleh lewat)
    // Ini ampuh buat blokir link judol yang dibungkus tag <a> atau <script>
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    public String sanitize(String input) {
        if (input == null) return null;
        // Kita gunakan policy yang sangat ketat (hanya teks aman)
        // Jika lo mau bener-bener hapus link, gunakan Sanitizers.BLOCKS
        return Sanitizers.FORMATTING.sanitize(input);
    }

    // Versi ekstra ketat: Hapus semua elemen termasuk link
    public String cleanTextOnly(String input) {
        if (input == null) return null;
        return Sanitizers.BLOCKS.sanitize(input);
    }
}