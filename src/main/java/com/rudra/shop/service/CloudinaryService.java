package com.rudra.shop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    // This tells Spring to use the secure URL from CloudinaryConfig!
    @Autowired
    private Cloudinary cloudinary;

    public String uploadImages(MultipartFile[] files) {
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try {
                // Now it uses the injected, correctly configured cloudinary instance
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                uploadedUrls.add(uploadResult.get("secure_url").toString());
            } catch (Exception e) {
                System.out.println("=================================================");
                System.out.println("CLOUDINARY UPLOAD ERROR ON FILE: " + file.getOriginalFilename());
                System.out.println("ERROR TYPE: " + e.getClass().getName());
                System.out.println("ERROR MESSAGE: " + e.getMessage());
                System.out.println("=================================================");
                throw new RuntimeException("Cloudinary Error: " + e.getMessage());
            }
        }

        // Joins multiple URLs with a comma for your database
        return String.join(",", uploadedUrls);
    }
}