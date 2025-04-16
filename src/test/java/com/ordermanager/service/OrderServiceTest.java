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
import java.time.LocalDateTime;
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
        Order order1 = new Order();
        order1.setOrderItems(List.of(new OrderItem())); // Initialize orderItems

        Order order2 = new Order();
        order2.setOrderItems(List.of(new OrderItem())); // Initialize orderItems

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

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
        final String productName = "Test Create Product";
        Order order = new Order();
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productName);
        product.setInventoryCount(10);
        product.setPrice(BigDecimal.valueOf(100));
        item.setProductName(productName);
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertEquals(BigDecimal.valueOf(200), result.getTotalAmount());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderWithInsufficientStock() {
        final String productName = "Test Create Product";
        Order order = new Order();
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productName);
        product.setInventoryCount(1);
        product.setPrice(BigDecimal.valueOf(100));
        item.setProductName(productName);
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));

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

    @Test
    void shouldSearchOrdersByNameAndDescription() {
        String name = "Test Product";
        String description = "Test Description";

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setOrderItems(List.of(new OrderItem()));

        when(orderRepository.findByProductNameAndDescription(name, description)).thenReturn(List.of(order));

        List<Order> result = orderService.searchOrdersByNameAndDescription(name, description);

        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository).findByProductNameAndDescription(name, description);
    }

    @Test
    void shouldThrowExceptionWhenNoOrdersFoundByNameAndDescription() {
        String name = "Nonexistent Product";
        String description = "Nonexistent Description";

        when(orderRepository.findByProductNameAndDescription(name, description)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> orderService.searchOrdersByNameAndDescription(name, description));
        verify(orderRepository).findByProductNameAndDescription(name, description);
    }

    @Test
    void shouldFilterOrdersByDateRange() {
        String startDate = "2023-01-01T00:00:00";
        String endDate = "2023-12-31T23:59:59";

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setOrderItems(List.of(new OrderItem()));

        when(orderRepository.findByDateRange(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)))
                .thenReturn(List.of(order));

        List<Order> result = orderService.filterOrdersByDateRange(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository).findByDateRange(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Test
    void shouldThrowExceptionWhenNoOrdersFoundInDateRange() {
        String startDate = "2023-01-01T00:00:00";
        String endDate = "2023-12-31T23:59:59";

        when(orderRepository.findByDateRange(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)))
                .thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> orderService.filterOrdersByDateRange(startDate, endDate));
        verify(orderRepository).findByDateRange(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }
}