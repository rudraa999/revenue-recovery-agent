package com.rudra.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.rudra.shop.model.Product;
import com.rudra.shop.repository.ProductRepository;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/")
    public String viewHomePage(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            Model model) {
        List<Product> listProducts;
        if ((keyword == null || keyword.isEmpty()) && (category == null || category.isEmpty())) {
            listProducts = productRepository.findByInCollectionTrueAndDeletedFalseOrderByDisplayOrderAscIdDesc();
        } else {
            listProducts = productRepository.searchProducts(keyword == null ? "" : keyword, category == null ? "" : category, "");
        }

        List<String> listCategories = productRepository.findAllCategories();

        // List<Product> trendingProducts =
        // productRepository.findByTrendingTrueOrderByDisplayOrderAsc();
        List<Product> trendingProducts = productRepository.findByTrendingTrueAndDeletedFalseOrderByDisplayOrderAscIdDesc();

        List<Map<String, String>> categoryDisplayList = new ArrayList<>();
        categoryDisplayList.add(Map.of("name", "Candles", "icon", "🕯️"));
        categoryDisplayList.add(Map.of("name", "Chenille Flowers", "icon", "🌸"));
        categoryDisplayList.add(Map.of("name", "Ribbon Flowers", "icon", "🎀"));
        categoryDisplayList.add(Map.of("name", "Resin Art", "icon", "💎"));
        categoryDisplayList.add(Map.of("name", "Lippan Art", "icon", "🪞"));
        categoryDisplayList.add(Map.of("name", "Texture Art", "icon", "🌙"));
        categoryDisplayList.add(Map.of("name", "Dream Catchers", "icon", "🪶"));
        categoryDisplayList.add(Map.of("name", "Create Hamper", "icon", "🎁"));
        categoryDisplayList.add(Map.of("name", "Rangoli Mat", "icon", "🏵️"));
        categoryDisplayList.add(Map.of("name", "Keychains", "icon", "🔑"));
        categoryDisplayList.add(Map.of("name", "Gifts for Boys", "icon", "🏎️"));
        categoryDisplayList.add(Map.of("name", "Hand Painted Frames", "icon", "🖼️"));

        model.addAttribute("listProducts", listProducts);
        model.addAttribute("listCategories", listCategories);
        model.addAttribute("categoryDisplayList", categoryDisplayList);
        model.addAttribute("trendingProducts", trendingProducts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);

        return "index";
    }

    @GetMapping("/product/{id}")
    public String viewProductDetails(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);
        return "product_details";
    }

    @GetMapping("/products")
    public String viewCategoryProducts(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "subcategory", required = false) String subcategory,
            Model model) {
        
        List<Product> listProducts = productRepository.searchProducts("", category == null ? "" : category, subcategory == null ? "" : subcategory);
        
        List<String> listSubcategories = new ArrayList<>();
        if (category != null && !category.isEmpty()) {
            listSubcategories = productRepository.findDistinctSubcategoriesByCategory(category);
        }

        model.addAttribute("listProducts", listProducts);
        model.addAttribute("listSubcategories", listSubcategories);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSubcategory", subcategory);
        
        return "products";
    }
}