package com.group5.afes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả các API
                // Dùng allowedOriginPatterns thay cho allowedOrigins để tránh lỗi khi dùng Credentials
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // Cho phép gửi kèm Cookie/Auth Header
                .maxAge(3600); // Lưu cache cấu hình CORS trong 1 giờ
    }
}