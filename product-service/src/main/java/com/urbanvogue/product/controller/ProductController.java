package com.urbanvogue.product.controller;

import com.urbanvogue.product.model.Product;
import com.urbanvogue.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Product deleted successfully!";
    }

    // This endpoint is called internally by Order Service only

    @PutMapping("/{id}/reduce-stock")
    public String reduceStock(@PathVariable Long id,
                              @RequestParam Integer quantity) {
        productService.reduceStock(id, quantity);
        return "Stock updated successfully";
    }

    // Called when customer cancels an order — puts items back on shelf
    @PutMapping("/{id}/restore-stock")
    public String restoreStock(@PathVariable Long id,
                               @RequestParam Integer quantity) {
        productService.restoreStock(id, quantity);
        return "Stock restored successfully";
    }
}