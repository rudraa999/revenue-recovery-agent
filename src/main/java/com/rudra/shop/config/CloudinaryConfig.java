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
            // Safe fallback for testing and development environments
            cloudinaryUrl = "cloudinary://123456789012345:dummy_secret@dummy-cloud";
        }

        return new Cloudinary(cloudinaryUrl);
    }
}
