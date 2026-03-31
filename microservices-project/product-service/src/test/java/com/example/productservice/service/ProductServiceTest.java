package com.example.productservice.service;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.dto.ProductVariantRequest;
import com.example.productservice.model.Product;
import com.example.productservice.model.ProductVariant;
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

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 13");
        request.setDescription("iPhone 13");
        request.setCategory("ao-thun");
        request.setVariants(List.of(buildVariantRequest("ts001_black_m", BigDecimal.valueOf(1200), "M", "black")));

        Product saved = new Product();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setDescription(request.getDescription());
        saved.setCategory(request.getCategory());
        saved.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(1200), "M", "black")));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response.getId());
        assertEquals("iPhone 13", response.getName());
        assertEquals("ao-thun", response.getCategory());
        assertEquals(1, response.getVariants().size());
        assertEquals("ts001_black_m", response.getVariants().get(0).getSkuCode());
        assertEquals(BigDecimal.valueOf(1200), response.getVariants().get(0).getPrice());
    }

    @Test
    void getAllProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 13");
        product.setDescription("iPhone 13");
        product.setCategory("ao-thun");
        product.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(1200), "M", "black")));

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
        product.setCategory("ao-thun");
        product.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(199), "M", "black")));

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
        existingProduct.setCategory("ao-thun");
        existingProduct.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(199), "M", "black")));

        ProductRequest request = new ProductRequest();
        request.setName("Ao thun oversize");
        request.setDescription("Ao thun oversize cotton");
        request.setCategory("ao-thun");
        request.setVariants(List.of(buildVariantRequest("ts001_white_l", BigDecimal.valueOf(249), "L", "white")));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(productId, request);

        assertEquals("Ao thun oversize", response.getName());
        assertEquals(1, response.getVariants().size());
        assertEquals(BigDecimal.valueOf(249), response.getVariants().get(0).getPrice());
        assertEquals("ts001_white_l", response.getVariants().get(0).getSkuCode());
        assertEquals("L", response.getVariants().get(0).getSize());
        assertEquals("white", response.getVariants().get(0).getColor());
    }

    @Test
    void deleteProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Ao thun basic");
        product.setDescription("Ao thun cotton");
        product.setCategory("ao-thun");
        product.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(199), "M", "black")));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).delete(product);
    }

    private ProductVariantRequest buildVariantRequest(String skuCode, BigDecimal price, String size, String color) {
        ProductVariantRequest request = new ProductVariantRequest();
        request.setSkuCode(skuCode);
        request.setPrice(price);
        request.setSize(size);
        request.setColor(color);
        request.setImageUrl("/assets/products/" + skuCode + ".jpg");
        return request;
    }

    private ProductVariant buildVariant(String skuCode, BigDecimal price, String size, String color) {
        ProductVariant variant = new ProductVariant();
        variant.setSkuCode(skuCode);
        variant.setPrice(price);
        variant.setSize(size);
        variant.setColor(color);
        variant.setImageUrl("/assets/products/" + skuCode + ".jpg");
        return variant;
    }
}
