package com.example.productservice.service;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.dto.ProductVariantRequest;
import com.example.productservice.dto.ProductVariantResponse;
import com.example.productservice.model.Product;
import com.example.productservice.model.ProductVariant;
import com.example.productservice.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    public ProductService(ProductRepository productRepository, ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);

        Product saved = productRepository.save(product);
        productEventPublisher.publishProductCreated(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        return toResponse(getExistingProduct(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(String category, String size, String color) {
        return productRepository.findAll()
            .stream()
            .filter(product -> matches(product.getCategory(), category))
            .filter(product -> matchesVariant(product, size, color))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = getExistingProduct(productId);
        applyRequest(product, request);
        Product saved = productRepository.save(product);
        productEventPublisher.publishProductUpdated(saved);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getExistingProduct(productId);
        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {
        List<ProductVariantResponse> variants = product.getVariants()
            .stream()
            .map(this::toVariantResponse)
            .collect(Collectors.toList());
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            variants,
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    private boolean matches(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return value != null && value.equalsIgnoreCase(filter);
    }

    private boolean matchesVariant(Product product, String size, String color) {
        if ((size == null || size.isBlank()) && (color == null || color.isBlank())) {
            return true;
        }
        return product.getVariants().stream()
            .anyMatch(variant -> matches(variant.getSize(), size) && matches(variant.getColor(), color));
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
            variant.getId(),
            variant.getSkuCode(),
            variant.getSize(),
            variant.getColor(),
            variant.getImageUrl(),
            variant.getPrice(),
            variant.getCreatedAt(),
            variant.getUpdatedAt()
        );
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setVariants(buildVariants(request));
    }

    private List<ProductVariant> buildVariants(ProductRequest request) {
        List<ProductVariantRequest> variantRequests = request.getVariants();
        if (variantRequests == null || variantRequests.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Product must contain at least one variant");
        }
        return variantRequests.stream()
            .map(this::toVariant)
            .collect(Collectors.toList());
    }

    private ProductVariant toVariant(ProductVariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setSkuCode(request.getSkuCode());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setImageUrl(request.getImageUrl());
        variant.setPrice(request.getPrice());
        return variant;
    }

    private Product getExistingProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }
}
