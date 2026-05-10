package com.inventory.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    public Long getId() { return id; }

    public String getCategoryName() { return categoryName; }

    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<Product> getProducts() { return products; }

    public void setProducts(List<Product> products) { this.products = products; }
}