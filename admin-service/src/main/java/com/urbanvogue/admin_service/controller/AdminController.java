package com.urbanvogue.admin_service.controller;

import com.urbanvogue.admin_service.model.Product;
import com.urbanvogue.admin_service.service.AdminService;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

   //dashboard
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }

    //get all products

    @GetMapping("/public/products")
    public ResponseEntity<?> publicProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    // create or add products
    @PostMapping("/products")
    public ResponseEntity<?> create(@RequestBody Product product) {
        return ResponseEntity.ok(service.createProduct(product));
    }

    // restock when new shipment arrives, or manually by admin or order cancells
    @PutMapping("/products/restock")
    public ResponseEntity<?> restock(
            @RequestParam String category,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                service.restockCategory(category, quantity)
        );
    }

    // reduce stock automatically when order is placed, or manually by admin
    @PutMapping("/products/reduce")
    public ResponseEntity<?> reduce(
            @RequestParam String category,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                service.reduceCategory(category, quantity)
        );
    }

    // apply bulk discounts based on category
    @PutMapping("/products/discount")
    public ResponseEntity<?> discount(
            @RequestParam String category,
            @RequestParam double percent) {

        return ResponseEntity.ok(
                service.applyBulkDiscount(category, percent)
        );
    }

    // delete or remove
    @DeleteMapping("/products/category")
    public ResponseEntity<?> delete(@RequestParam String category) {
        return ResponseEntity.ok(service.deleteByCategory(category));
    }


    // GET BY ID
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET BY CATEGORY
    @GetMapping("/products/category")
    public ResponseEntity<?> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(service.getByCategory(category));
    }

    // GET BY PRICE RANGE
    @GetMapping("/products/price-range")
    public ResponseEntity<?> priceRange(
            @RequestParam double min,
            @RequestParam double max) {

        return ResponseEntity.ok(service.getByPriceRange(min, max));
    }

    // LOW STOCK
    @GetMapping("/products/low-stock")
    public ResponseEntity<?> lowStock(@RequestParam int qty) {
        return ResponseEntity.ok(service.getLowStock(qty));
    }

    // SEARCH FOR A PRODUCT BY NAME
    @GetMapping("/products/search")
    public ResponseEntity<?> search(@RequestParam String name) {
        return ResponseEntity.ok(service.searchByName(name));
    }
}