package com.nezra.gadgetinventory.services;

import com.nezra.gadgetinventory.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository <Product, Integer> {
}
