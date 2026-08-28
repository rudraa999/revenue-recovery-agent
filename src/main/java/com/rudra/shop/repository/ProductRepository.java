package com.rudra.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rudra.shop.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDeletedFalseOrderByDisplayOrderAscIdDesc();

    // List<Product> findByTrendingTrueOrderByDisplayOrderAsc();
    List<Product> findByTrendingTrueAndDeletedFalseOrderByDisplayOrderAscIdDesc();
    List<Product> findByInCollectionTrueAndDeletedFalseOrderByDisplayOrderAscIdDesc();

    List<Product> findAllByOrderByDisplayOrderAscIdDesc();

    Product findByName(String name);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:subcategory IS NULL OR :subcategory = '' OR LOWER(p.subcategory) = LOWER(:subcategory)) " +
            "ORDER BY p.displayOrder ASC, p.id DESC")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("category") String category, @Param("subcategory") String subcategory);

    @Query("SELECT DISTINCT p.subcategory FROM Product p WHERE p.deleted = false AND LOWER(p.category) = LOWER(:category) AND p.subcategory IS NOT NULL AND p.subcategory != ''")
    List<String> findDistinctSubcategoriesByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.deleted = false")
    List<String> findAllCategories();
}