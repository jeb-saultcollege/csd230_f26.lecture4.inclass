package csd230.lab1.controllers;

import csd230.lab1.entities.OrderEntity;
import csd230.lab1.repositories.OrderEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders REST API", description = "JSON API for managing orders")
@RestController
@RequestMapping("/api/rest/orders")
public class OrderRestController {
    private final OrderEntityRepository repository;

    public OrderRestController(OrderEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List orders")
    @ApiResponse(responseCode = "200", description = "Orders returned")
    public List<OrderEntity> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order")
    @ApiResponse(responseCode = "200", description = "Order returned")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public OrderEntity one(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @PostMapping
    @Operation(summary = "Create an order")
    @ApiResponse(responseCode = "200", description = "Order created")
    public OrderEntity create(@RequestBody OrderEntity order) {
        return repository.save(order);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an order")
    @ApiResponse(responseCode = "200", description = "Order replaced")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public OrderEntity replace(@RequestBody OrderEntity replacement, @PathVariable Long id) {
        OrderEntity order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setTotalAmount(replacement.getTotalAmount());
        order.setOrderDate(replacement.getOrderDate());
        order.setProducts(replacement.getProducts());
        return repository.save(order);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order")
    @ApiResponse(responseCode = "200", description = "Order deleted")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
