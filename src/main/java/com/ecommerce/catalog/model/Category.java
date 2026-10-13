package com.ecommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 30)
    private String vertical; // "grocery", "general", "electronics" — for grouping/filtering

    public Category() {}

    public Category(String name, String vertical) {
        this.name = name;
        this.vertical = vertical;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVertical() { return vertical; }
    public void setVertical(String vertical) { this.vertical = vertical; }
}