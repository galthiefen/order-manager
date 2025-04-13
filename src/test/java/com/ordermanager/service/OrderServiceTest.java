package com.ordermanager.service;

import com.ordermanager.model.Order;
import com.ordermanager.model.OrderItem;
import com.ordermanager.model.Product;
import com.ordermanager.repository.OrderRepository;
import com.ordermanager.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingProduct = new Product();
        existingProduct.setProductId(UUID.randomUUID());
        existingProduct.setName("Test Product");
        existingProduct.setCategory("Test Category");
        existingProduct.setPrice(BigDecimal.valueOf(100));
        existingProduct.setInventoryCount(3);
    }

    @Test
    void shouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order(), new Order()));

        List<Order> orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        verify(orderRepository).findAll();
    }

    @Test
    void shouldReturnOrderById() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(orderId);

        assertEquals(order, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundById() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldCreateOrder() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setInventoryCount(10);
        product.setPrice(BigDecimal.valueOf(100));
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertEquals(BigDecimal.valueOf(200), result.getTotalAmount());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderWithInsufficientStock() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setInventoryCount(2);
        item.setProduct(product);
        item.setQuantity(5);
        order.setOrderItems(List.of(item));

        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));
        verify(productRepository, never()).save(product);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void shouldUpdateOrder() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        OrderItem existingItem = new OrderItem();
        existingItem.setProduct(existingProduct);
        existingItem.setQuantity(2);
        existingOrder.setOrderItems(List.of(existingItem));

        Order updatedOrder = new Order();
        OrderItem updatedItem = new OrderItem();
        updatedItem.setProduct(existingProduct);
        updatedItem.setQuantity(3);
        updatedOrder.setOrderItems(List.of(updatedItem));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(productRepository.findById(existingProduct.getProductId())).thenReturn(Optional.of(existingProduct));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        Order result = orderService.updateOrder(orderId, updatedOrder);

        assertEquals(BigDecimal.valueOf(300), result.getTotalAmount());
        verify(productRepository, times(2)).save(existingProduct);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingOrderWithInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        OrderItem existingItem = new OrderItem();
        existingItem.setProduct(existingProduct);
        existingItem.setQuantity(2);
        existingOrder.setOrderItems(List.of(existingItem));

        Order updatedOrder = new Order();
        OrderItem updatedItem = new OrderItem();
        updatedItem.setProduct(existingProduct);
        updatedItem.setQuantity(15);
        updatedOrder.setOrderItems(List.of(updatedItem));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(productRepository.findById(existingProduct.getProductId())).thenReturn(Optional.of(existingProduct));

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrder(orderId, updatedOrder));
        verify(productRepository, times(1)).save(existingProduct);
        verify(orderRepository, never()).save(existingOrder);
    }

    @Test
    void shouldDeleteOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setInventoryCount(10);
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));

        orderService.deleteOrder(orderId);

        verify(productRepository).save(product);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(orderId));
        verify(orderRepository, never()).deleteById(orderId);
    }
}