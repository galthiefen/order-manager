package com.ordermanager.service;

import com.ordermanager.model.Order;
import com.ordermanager.model.OrderItem;
import com.ordermanager.model.Product;
import com.ordermanager.repository.OrderRepository;
import com.ordermanager.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null) {
                    item.setProductName(item.getProduct().getName());
                }
            }
        }
        return orders;
    }

    public Order getOrderById(UUID orderId) {
        return getOrderByOrderId(orderId);
    }

    @Transactional
    public Order createOrder(Order order) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findByName(item.getProductName())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + item.getProductName()));

            if (product.getInventoryCount() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            product.setInventoryCount(product.getInventoryCount() - item.getQuantity());
            productRepository.save(product);

            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(UUID orderId, Order updatedOrder) {
        Order existingOrder = getOrderByOrderId(orderId);

        for (OrderItem item : existingOrder.getOrderItems()) {
            Product product = getProductByProductId(item.getProductId());
            product.setInventoryCount(product.getInventoryCount() + item.getQuantity());
            productRepository.save(product);
        }

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
        existingOrder.setPaymentMethod(updatedOrder.getPaymentMethod());
        existingOrder.setNotes(updatedOrder.getNotes());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItem item : updatedOrder.getOrderItems()) {
            Product product = getProductByProductId(item.getProductId());

            if (product.getInventoryCount() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            product.setInventoryCount(product.getInventoryCount() - item.getQuantity());
            productRepository.save(product);

            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        existingOrder.setTotalAmount(totalAmount);
        return orderRepository.save(existingOrder);
    }

    public void deleteOrder(UUID orderId) {
        Order order = getOrderByOrderId(orderId);

        for (OrderItem item : order.getOrderItems()) {
            Product product = getProductByProductId(item.getProductId());
            product.setInventoryCount(product.getInventoryCount() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.deleteById(orderId);
    }

    private Order getOrderByOrderId(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId));
    }

    private Product getProductByProductId(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id: " + productId));
    }

    public List<Order> searchOrdersByNameAndDescription(String name, String description) {
        return orderRepository.findByProductNameAndDescription(name, description);
    }

    public List<Order> filterOrdersByDateRange(String startDate, String endDate) {
        LocalDateTime startDateTime = LocalDateTime.parse(startDate);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate);
        try {
            return orderRepository.findByDateRange(startDateTime, endDateTime);
        } catch (Exception e) {
            logger.error("Error occurred while filtering orders by date range: {}", e.getMessage());
            throw new RuntimeException("Failed to filter orders by date range", e);
        }
    }

}