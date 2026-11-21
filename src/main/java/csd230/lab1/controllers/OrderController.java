package csd230.lab1.controllers;
import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Tag(name = "Orders", description = "Operations related to Order Management")
@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private CartEntityRepository cartRepository;
    @Autowired
    private BookEntityRepository bookRepository;
    @Autowired
    private OrderEntityRepository orderRepository;
    @Autowired
    private ProductEntityRepository productEntityRepository;

    // 1. View the contents of the cart
    @Operation(summary = "Get all orders", description = "Returns the HTML view of the order list")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "orderDetails";
    }
}
