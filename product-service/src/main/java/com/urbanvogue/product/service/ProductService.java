package com.urbanvogue.product.service;

import com.urbanvogue.product.model.Product;
import com.urbanvogue.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // READ ALL
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // READ ONE
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // CREATE
    public Product addProduct(Product product) {
        // --- START BUSINESS LOGIC ---

        // 1. Rule: Price must be greater than zero
        if (product.getPrice() <= 0) {
            throw new RuntimeException("Price must be a positive value for UrbanVogue products.");
        }

        // 2. Rule: Stock cannot be negative
        if (product.getStockQuantity() < 0) {
            throw new RuntimeException("Initial stock cannot be negative.");
        }

        // 3. Rule: Format name to Uppercase (Company Standard)
        product.setName(product.getName().toUpperCase());

        // --- END BUSINESS LOGIC ---
        return productRepository.save(product);
    }

    // UPDATE
    public Product updateProduct(Long id, Product newDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(newDetails.getName());
            product.setPrice(newDetails.getPrice());
            product.setStockQuantity(newDetails.getStockQuantity());
            product.setDescription(newDetails.getDescription());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    // DELETE
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}