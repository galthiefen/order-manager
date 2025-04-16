package com.ordermanager.controller;

import com.ordermanager.dto.DateRangeRequestDTO;
import com.ordermanager.dto.NameDescriptionRequestDTO;
import com.ordermanager.model.Order;
import com.ordermanager.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable UUID orderId, @RequestBody Order updatedOrder) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, updatedOrder));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search-by-name-description")
    public ResponseEntity<List<Order>> searchOrdersByNameAndDescription(@RequestBody NameDescriptionRequestDTO nameDescriptionRequest) {
        return ResponseEntity.ok(orderService.searchOrdersByNameAndDescription(nameDescriptionRequest.getName(), nameDescriptionRequest.getDescription()));
    }

    @GetMapping("/filter-by-date-range")
    public ResponseEntity<List<Order>> filterOrdersByDateRange(@RequestBody DateRangeRequestDTO dateRangeRequest) {
        return ResponseEntity.ok(orderService.filterOrdersByDateRange(dateRangeRequest.getStartDate(), dateRangeRequest.getEndDate()));
    }
}