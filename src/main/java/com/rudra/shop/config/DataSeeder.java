package com.rudra.shop.config;

import com.rudra.shop.model.PromoCode;
import com.rudra.shop.model.User;
import com.rudra.shop.repository.PromoCodeRepository;
import com.rudra.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername(adminUsername) == null) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminUsername + "@shop.com");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("New Admin Initialized: " + adminUsername);
        }

        // Seed initial Promo Codes
        if (promoCodeRepository.count() == 0) {
            PromoCode p1 = new PromoCode();
            p1.setCode("WELCOME10");
            p1.setDescription("10% Flat Discount for new customers");
            p1.setDiscountType("PERCENTAGE");
            p1.setDiscountValue(10.0);
            p1.setMinOrderAmount(500.0);
            p1.setMaxDiscountAmount(1500.0);
            p1.setMaxUses(5000);
            p1.setActive(true);
            p1.setExpiryDate(LocalDateTime.now().plusMonths(6));
            promoCodeRepository.save(p1);

            PromoCode p2 = new PromoCode();
            p2.setCode("ART50");
            p2.setDescription("₹50 Flat OFF on handcrafted art");
            p2.setDiscountType("FIXED");
            p2.setDiscountValue(50.0);
            p2.setMinOrderAmount(300.0);
            p2.setMaxUses(1000);
            p2.setActive(true);
            p2.setExpiryDate(LocalDateTime.now().plusMonths(3));
            promoCodeRepository.save(p2);

            PromoCode p3 = new PromoCode();
            p3.setCode("FESTIVE15");
            p3.setDescription("15% Festive Season Discount");
            p3.setDiscountType("PERCENTAGE");
            p3.setDiscountValue(15.0);
            p3.setMinOrderAmount(2000.0);
            p3.setMaxDiscountAmount(3000.0);
            p3.setMaxUses(2000);
            p3.setActive(true);
            p3.setExpiryDate(LocalDateTime.now().plusMonths(4));
            promoCodeRepository.save(p3);

            System.out.println("Initial Promo Codes Seeded: WELCOME10, ART50, FESTIVE15");
        }
    }
}
