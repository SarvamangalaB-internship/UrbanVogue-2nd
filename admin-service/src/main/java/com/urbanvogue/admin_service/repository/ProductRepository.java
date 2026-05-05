package com.urbanvogue.admin_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.transaction.Transactional;

import com.urbanvogue.admin_service.model.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByPriceBetween(Double min, Double max);

    List<Product> findByStockQuantityLessThan(Integer qty);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    void deleteByCategoryIgnoreCase(String category);
}