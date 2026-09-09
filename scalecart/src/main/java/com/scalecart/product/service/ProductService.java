package com.scalecart.product.service;

import com.scalecart.product.dto.ProductRequest;
import com.scalecart.product.dto.ProductResponse;
import com.scalecart.product.entity.Product;
import com.scalecart.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.scalecart.exception.ProductNotFoundException;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Product with SKU already exists");
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    private ProductResponse mapToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setSku(product.getSku());
        response.setCategory(product.getCategory());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        return mapToResponse(product);
    }
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        ));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        ));

        productRepository.delete(product);
    }
}
