package com.example.ecommercial.domain.dto.response;

import com.example.ecommercial.domain.entity.OrderEntity;
import com.example.ecommercial.domain.entity.ProductCategoryEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductGetResponse {
    private Long id;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String lastModifiedBy;
    private String name;
    private String description;
    private double price;
    private int amount;
    private ProductCategoryEntity categories;
    private List<OrderEntity> orderEntities;
}