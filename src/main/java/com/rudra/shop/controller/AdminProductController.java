package com.rudra.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rudra.shop.model.Product;
import com.rudra.shop.repository.ProductRepository;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.rudra.shop.service.CloudinaryService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findByDeletedFalseOrderByDisplayOrderAscIdDesc());
        return "admin/products";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/add_product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        System.out.println("========== ADD PRODUCT FORM SUBMITTED ==========");
        System.out.println("Product Name: " + product.getName());

        if (imageFiles == null) {
            System.err.println(
                    "CRITICAL: imageFiles is NULL! The form didn't send the files correctly or Spring couldn't parse them.");
        } else {
            System.out.println("Received " + imageFiles.length + " files.");
            for (int i = 0; i < imageFiles.length; i++) {
                MultipartFile file = imageFiles[i];
                System.out.println("File " + i + ": Name=" + file.getOriginalFilename() + ", Size=" + file.getSize()
                        + ", IsEmpty=" + file.isEmpty());
            }
        }

        boolean hasImages = false;
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    hasImages = true;
                    break;
                }
            }
        }

        if (hasImages) {
            System.out.println("Uploading images to Cloudinary...");
            String imageUrls = cloudinaryService.uploadImages(imageFiles);
            System.out.println("Cloudinary returned URLs: [" + imageUrls + "]");
            product.setImageUrls(imageUrls);
        } else {
            System.out.println("Skipping Cloudinary upload because no valid images were provided.");
        }

        System.out.println("Saving product to database...");
        productRepository.save(product);
        System.out.println("Product saved with ID: " + product.getId() + " and Image URLs: " + product.getImageUrls());
        System.out.println("=================================================");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "admin/edit-product";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, @ModelAttribute Product updatedProduct,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setSubcategory(updatedProduct.getSubcategory());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setTrending(updatedProduct.isTrending());
        existingProduct.setInCollection(updatedProduct.isInCollection());
        existingProduct.setDisplayOrder(updatedProduct.getDisplayOrder());

        boolean hasImages = false;
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    hasImages = true;
                    break;
                }
            }
        }

        if (hasImages) {
            String imageUrls = cloudinaryService.uploadImages(imageFiles);
            existingProduct.setImageUrls(imageUrls);
        }

        productRepository.save(existingProduct);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        product.setDeleted(true);
        productRepository.save(product);
        return "redirect:/admin/products";
    }
}
