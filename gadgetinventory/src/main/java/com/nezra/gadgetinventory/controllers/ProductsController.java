package com.nezra.gadgetinventory.controllers;


import com.nezra.gadgetinventory.models.Product;
import com.nezra.gadgetinventory.models.ProductEdit;

import com.nezra.gadgetinventory.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);
        return "products/index";

    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductEdit productEdit = new ProductEdit();
        model.addAttribute("productEdit", productEdit);
        return "products/createproduct";
    }

    @PostMapping("/create")
    public String createPage(@Valid @ModelAttribute ProductEdit productEdit, BindingResult result) {
        if (productEdit.getImageFile().isEmpty()) {
            result.addError(new FieldError("productEdit", "imageFile", "The image file is required"));
        }
        if (result.hasErrors()) {
            return "products/createproduct";
        }

        MultipartFile image = productEdit.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDirectory = "public/images/";
            Path uploadPath = Paths.get(uploadDirectory);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDirectory + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productEdit.getName());
        product.setBrand(productEdit.getBrand());
        product.setCategory(productEdit.getCategory());
        product.setPrice(productEdit.getPrice());
        product.setDescription(productEdit.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductEdit productEdit = new ProductEdit();
            productEdit.setName(productEdit.getName());
            productEdit.setBrand(productEdit.getBrand());
            productEdit.setCategory(productEdit.getCategory());
            productEdit.setPrice(productEdit.getPrice());
            productEdit.setDescription(productEdit.getDescription());

            model.addAttribute("productEdit", productEdit);
        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
            return "redirect/products";
        }
        return "products/editproduct";


    }

    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute
            ProductEdit productEdit,
            BindingResult bindingResult) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (bindingResult.hasErrors()) {
                return "products/editproduct";
            }
            if (!productEdit.getImageFile().isEmpty()) {

                String uploadDirectory = "public/images/";
                Path oldImagePath = Paths.get(uploadDirectory + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception" + ex.getMessage());
                }

                MultipartFile image = productEdit.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDirectory + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            product.setName(productEdit.getName());
            product.setBrand(productEdit.getBrand());
            product.setCategory(productEdit.getCategory());
            product.setPrice(productEdit.getPrice());
            product.setDescription(productEdit.getDescription());

            repo.save(product);
        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception" + ex.getMessage());
            }
            repo.delete(product);
        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
        }
        return "redirect:/products";
    }
}
