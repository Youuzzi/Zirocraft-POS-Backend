package com.zirocraft.billingsoftware.config;

import com.zirocraft.billingsoftware.filter.JwtRequestFilter;
import com.zirocraft.billingsoftware.service.impl.AppUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailService appUserDetailService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        // --- SIGNATURE SAKTI ZIROCRAFT STUDIO ---
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("X-Powered-By", "Zirocraft-Studio-ID");
                            response.setHeader("X-Engine-Author", "zirocraftid@gmail.com");
                        })
                        // CONTENT SECURITY POLICY (Mencegah Injeksi Script/Judol)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                        "img-src 'self' data: http://localhost:8080; " +
                                        "font-src 'self' https://fonts.gstatic.com;")))
                .authorizeHttpRequests(auth -> auth
                        // 1. Jalur Publik (Tanpa Login)
                        .requestMatchers("/login", "/encode", "/uploads/**").permitAll()

                        // 2. Jalur Akses Data Dasar (Bisa Admin & Kasir/User)
                        // GET categories, items, dan settings diizinkan agar Kasir bisa fetch menu & modal awal
                        .requestMatchers(HttpMethod.GET, "/categories/**", "/items/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/admin/settings/**").hasAnyRole("ADMIN", "USER")

                        // 3. Jalur Sesi Shift (Admin & Kasir harus bisa buka/tutup shift)
                        .requestMatchers("/shifts/**").hasAnyRole("ADMIN", "USER")

                        // 4. Jalur Management (Hanya Admin)
                        // Segala sesuatu di bawah /admin/ (kecuali GET settings tadi) wajib ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 5. Selebihnya wajib ter-autentikasi
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Izinkan Frontend React lo
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(appUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }
}