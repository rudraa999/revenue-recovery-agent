package com.rudra.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE products MODIFY image_urls TEXT");
            System.out.println("SUCCESSFULLY ALTERED image_urls COLUMN TO TEXT");
        } catch (Exception e) {
            System.out.println("Could not alter column: " + e.getMessage());
        }
    }
}
