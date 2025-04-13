package com.ordermanager.service;

import com.ordermanager.model.Product;
import com.ordermanager.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");
        product.setCategory("Test Category");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setInventoryCount(10);
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts_whenProductsExist() {

        Product product1 = new Product();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setCategory("Category 1");
        product1.setPrice(BigDecimal.valueOf(20.00));
        product1.setInventoryCount(5);

        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setCategory("Category 2");
        product2.setPrice(BigDecimal.valueOf(30.00));
        product2.setInventoryCount(3);

        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_found() {
        when(productRepository.findById(product.getProductId()))
                .thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(product.getProductId());

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository, times(1)).findById(product.getProductId());
    }

    @Test
    void testGetProductById_notFound() {
        UUID fakeId = UUID.randomUUID();
        when(productRepository.findById(fakeId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(fakeId);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(fakeId);
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product created = productService.createProduct(product);

        assertNotNull(created);
        assertEquals(product.getName(), created.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenProductExists() {
        Product existingProduct = new Product();
        existingProduct.setProductId(UUID.randomUUID());
        existingProduct.setName("Old Product");
        existingProduct.setCategory("Old Category");
        existingProduct.setPrice(BigDecimal.valueOf(50.00));
        existingProduct.setInventoryCount(20);

        Product updatedProduct = new Product();
        updatedProduct.setProductId(existingProduct.getProductId());
        updatedProduct.setName("Updated Product");
        updatedProduct.setCategory("Updated Category");
        updatedProduct.setPrice(BigDecimal.valueOf(60.00));
        updatedProduct.setInventoryCount(15);

        when(productRepository.findById(existingProduct.getProductId()))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(existingProduct.getProductId(), updatedProduct);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("Updated Category", result.getCategory());
        assertEquals(BigDecimal.valueOf(60.00), result.getPrice());
        assertEquals(15, result.getInventoryCount());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();
        assertEquals("Updated Product", capturedProduct.getName());
        assertEquals("Updated Category", capturedProduct.getCategory());
        assertEquals(BigDecimal.valueOf(60.00), capturedProduct.getPrice());
        assertEquals(15, capturedProduct.getInventoryCount());
        verify(productRepository).findById(existingProduct.getProductId());
    }

    @Test
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {
        UUID fakeId = UUID.randomUUID();
        Product updatedProduct = new Product();
        updatedProduct.setProductId(fakeId);
        updatedProduct.setName("Updated Product");
        updatedProduct.setCategory("Updated Category");
        updatedProduct.setPrice(BigDecimal.valueOf(60.00));
        updatedProduct.setInventoryCount(15);

        when(productRepository.findById(fakeId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> productService.updateProduct(fakeId, updatedProduct));

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(fakeId);
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        UUID productId = UUID.randomUUID();
        Product existingProduct = new Product();
        existingProduct.setProductId(productId);
        existingProduct.setName("Product to Delete");
        existingProduct.setCategory("Category");
        existingProduct.setPrice(BigDecimal.valueOf(50.00));
        existingProduct.setInventoryCount(10);

        when(productRepository.existsById(productId)).thenReturn(true);
        productService.deleteProduct(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductNotFound() {
        UUID nonExistentProductId = UUID.randomUUID();

        when(productRepository.findById(nonExistentProductId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.deleteProduct(nonExistentProductId));
    }

}