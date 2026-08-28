package com.rudra.shop.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");

        if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
            System.err.println("CRITICAL: CLOUDINARY_URL is missing!");
            throw new IllegalStateException(
                    "Missing CLOUDINARY_URL Environment Variable. If you are using VS Code, make sure you are starting the application using the 'Run and Debug' view with the launch.json configuration, NOT the Spring Boot Dashboard. You also must completely restart the application.");
        }

        return new Cloudinary(cloudinaryUrl);
    }
}
