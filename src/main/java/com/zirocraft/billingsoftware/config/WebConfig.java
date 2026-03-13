package com.zirocraft.billingsoftware.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // PERHATIKAN: Kita arahkan ke folder yang ada gambar GOOGLE-nya
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/Users/HP/Documents/SpringBootPOS/backend/billingsoftware/src/main/resources/static/uploads/");
    }
}
