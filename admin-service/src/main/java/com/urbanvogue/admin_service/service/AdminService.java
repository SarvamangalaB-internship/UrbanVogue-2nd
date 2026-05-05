package com.urbanvogue.admin_service.service;

import com.urbanvogue.admin_service.model.Product;
import com.urbanvogue.admin_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {

    private final ProductRepository repo;

    public AdminService(ProductRepository repo) {
        this.repo = repo;
    }


    // GET ALL PRODUCTS

    public List<Product> getAllProducts() {
        return repo.findAll();
    }


    // CREATE PRODUCT

    public Product createProduct(Product product) {

        if (product.getPrice() == null || product.getPrice() <= 0)
            throw new RuntimeException("Invalid price");

        if (product.getStockQuantity() == null || product.getStockQuantity() < 0)
            throw new RuntimeException("Invalid stock");

        product.setName(product.getName().toUpperCase());

        return repo.save(product);
    }

    //  RESTOCK

    public String restockCategory(String category, int qty) {

        List<Product> list = repo.findByCategoryIgnoreCase(category);

        list.forEach(p ->
                p.setStockQuantity(
                        (p.getStockQuantity() == null ? 0 : p.getStockQuantity()) + qty
                )
        );

        repo.saveAll(list);

        return "Restocked successfully";
    }

    // REDUCE STOCK

    public String reduceCategory(String category, int qty) {

        List<Product> list = repo.findByCategoryIgnoreCase(category);

        list.forEach(p -> {
            int current = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            if (current >= qty) {
                p.setStockQuantity(current - qty);
            }
        });

        repo.saveAll(list);

        return "Stock reduced successfully";
    }


    // BULK DISCOUNT

    public String applyBulkDiscount(String category, double percent) {

        List<Product> list = repo.findByCategoryIgnoreCase(category);

        list.forEach(p -> {
            if (p.getPrice() != null) {
                double newPrice = p.getPrice() - (p.getPrice() * percent / 100);
                p.setPrice(newPrice);
            }
        });

        repo.saveAll(list);

        return "Discount applied successfully";
    }


    //  DELETE CATEGORY

    public String deleteByCategory(String category) {

        repo.deleteByCategoryIgnoreCase(category);

        return "Category deleted successfully";
    }

    // GET BY ID
    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // GET BY CATEGORY
    public List<Product> getByCategory(String category) {
        return repo.findByCategoryIgnoreCase(category);
    }

    // GET BY PRICE RANGE
    public List<Product> getByPriceRange(double min, double max) {
        return repo.findByPriceBetween(min, max);
    }

    // LOW STOCK
    public List<Product> getLowStock(int qty) {
        return repo.findByStockQuantityLessThan(qty);
    }

    // SEARCH NAME
    public List<Product> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    // DASHBOARD

    public Map<String, Object> getDashboardStats() {

        List<Product> products = repo.findAll();

        long totalProducts = products.size();

        long outOfStock = products.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() == 0)
                .count();

        double totalInventoryValue = products.stream()
                .filter(p -> p.getPrice() != null && p.getStockQuantity() != null)
                .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
                .sum();

        Map<String, Object> map = new HashMap<>();
        map.put("totalProducts", totalProducts);
        map.put("outOfStock", outOfStock);
        map.put("totalInventoryValue", totalInventoryValue);

        return map;
    }
}