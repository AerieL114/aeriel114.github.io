package com.example.productservice.service;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 13");
        request.setDescription("iPhone 13");
        request.setPrice(BigDecimal.valueOf(1200));
        request.setSkuCode("ts001_black_m");
        request.setCategory("ao-thun");
        request.setSize("M");
        request.setColor("black");
        request.setImageUrl("/assets/products/ts001_black_m.jpg");

        Product saved = new Product();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setDescription(request.getDescription());
        saved.setPrice(request.getPrice());
        saved.setSkuCode(request.getSkuCode());
        saved.setCategory(request.getCategory());
        saved.setSize(request.getSize());
        saved.setColor(request.getColor());
        saved.setImageUrl(request.getImageUrl());

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response.getId());
        assertEquals("iPhone 13", response.getName());
        assertEquals(BigDecimal.valueOf(1200), response.getPrice());
        assertEquals("ts001_black_m", response.getSkuCode());
        assertEquals("ao-thun", response.getCategory());
        assertEquals("M", response.getSize());
        assertEquals("black", response.getColor());
    }

    @Test
    void getAllProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 13");
        product.setDescription("iPhone 13");
        product.setPrice(BigDecimal.valueOf(1200));
        product.setSkuCode("ts001_black_m");
        product.setCategory("ao-thun");
        product.setSize("M");
        product.setColor("black");
        product.setImageUrl("/assets/products/ts001_black_m.jpg");

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(1, responses.size());
        assertEquals("iPhone 13", responses.get(0).getName());
    }

    @Test
    void getProductById() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Ao thun basic");
        product.setDescription("Ao thun cotton");
        product.setPrice(BigDecimal.valueOf(199));
        product.setSkuCode("ts001_black_m");
        product.setCategory("ao-thun");
        product.setSize("M");
        product.setColor("black");
        product.setImageUrl("/assets/products/ts001_black_m.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(productId);

        assertEquals(productId, response.getId());
        assertEquals("Ao thun basic", response.getName());
    }

    @Test
    void updateProduct() {
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Ao thun basic");
        existingProduct.setDescription("Ao thun cotton");
        existingProduct.setPrice(BigDecimal.valueOf(199));
        existingProduct.setSkuCode("ts001_black_m");
        existingProduct.setCategory("ao-thun");
        existingProduct.setSize("M");
        existingProduct.setColor("black");
        existingProduct.setImageUrl("/assets/products/ts001_black_m.jpg");

        ProductRequest request = new ProductRequest();
        request.setName("Ao thun oversize");
        request.setDescription("Ao thun oversize cotton");
        request.setPrice(BigDecimal.valueOf(249));
        request.setSkuCode("ts001_white_l");
        request.setCategory("ao-thun");
        request.setSize("L");
        request.setColor("white");
        request.setImageUrl("/assets/products/ts001_white_l.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(productId, request);

        assertEquals("Ao thun oversize", response.getName());
        assertEquals(BigDecimal.valueOf(249), response.getPrice());
        assertEquals("ts001_white_l", response.getSkuCode());
        assertEquals("L", response.getSize());
        assertEquals("white", response.getColor());
    }

    @Test
    void deleteProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Ao thun basic");
        product.setDescription("Ao thun cotton");
        product.setPrice(BigDecimal.valueOf(199));
        product.setSkuCode("ts001_black_m");
        product.setCategory("ao-thun");
        product.setSize("M");
        product.setColor("black");
        product.setImageUrl("/assets/products/ts001_black_m.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).delete(product);
    }
}
