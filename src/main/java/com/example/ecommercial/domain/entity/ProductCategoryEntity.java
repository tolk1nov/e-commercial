package com.example.ecommercial.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "categories")
public class ProductCategoryEntity extends BaseEntity{
    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductCategoryEntity categories;

    @OneToMany(mappedBy = "categories")
    private List<ProductCategoryEntity> productCategories;

    @OneToMany(mappedBy = "categories")
    private List<ProductEntity> productEntities;

    @OneToMany(mappedBy = "categories")
    private List<HistoryEntity> historyEntities;
}