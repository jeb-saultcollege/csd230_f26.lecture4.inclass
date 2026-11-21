package csd230.lab1.controllers;
import csd230.lab1.entities.*;
import csd230.lab1.repositories.*;
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

import java.security.Principal;
import java.time.LocalDateTime;

@Tag(name = "Cart", description = "Operations related to Shopping Cart Management")
@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartEntityRepository cartRepository;
    @Autowired
    private BookEntityRepository bookRepository;
    @Autowired
    private OrderEntityRepository orderRepository;
    @Autowired
    private ProductEntityRepository productRepository;
    @Autowired
    private UserEntityRepository userRepository;

    // Helper method to get the current user's cart
    private CartEntity getCartForCurrentUser(Principal principal) {
        // 1. Get username from security context
        String username = principal.getName();
        // 2. Find User in DB
        UserEntity user = userRepository.findByUsername(username);

        // 3. Find Cart for this User
        CartEntity cart = cartRepository.findByUser(user);

        // 4. If no cart exists, create one and link it to the user
        if (cart == null) {
            cart = new CartEntity();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        return cart;
    }


    // 1. View the contents of the cart
    @Operation(summary = "View cart", description = "Returns the HTML view of the current user's cart contents")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cart")
    @GetMapping
    public String viewCart(Model model, Principal principal) {
        // get cart based on Principal, not hard coded
        CartEntity cart = getCartForCurrentUser(principal);
        model.addAttribute("cart", cart);
        return "cartDetails";
    }
    // 2. Add a specific book to the cart
    @Operation(summary = "Add book to cart", description = "Adds the specified book to the current user's cart and redirects to the book list")
    @ApiResponse(responseCode = "302", description = "Book added (or not found), redirected to book list")
    @GetMapping("/add/{bookId}")
    public String addToCart(@PathVariable Long bookId, Principal principal) {
        CartEntity cart = getCartForCurrentUser(principal);
        BookEntity book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            cart.addProduct(book);
            cartRepository.save(cart);
        }
        return "redirect:/books";
    }


    // 3. Remove item from cart
    @Operation(summary = "Remove book from cart", description = "Removes the specified book from the current user's cart and redirects to the cart")
    @ApiResponse(responseCode = "302", description = "Book removed (or not found), redirected to cart")
    @GetMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Long bookId, Principal principal) {
        CartEntity cart = getCartForCurrentUser(principal);
        BookEntity book = bookRepository.findById(bookId).orElse(null);

        if (book != null) {
            cart.getProducts().remove(book);
            cartRepository.save(cart);
        }
        return "redirect:/cart";
    }


    // 4. checkout the cart
    @Operation(summary = "Checkout cart", description = "Creates an order from the current user's cart items, clears the cart, and redirects to the order view")
    @ApiResponse(responseCode = "302", description = "Checkout processed, redirected to order view")
    @PostMapping("/checkout")
    public String checkout(Principal principal) {
        //get principal cart
        CartEntity cart = getCartForCurrentUser(principal);

        //only if we have a cart
        if(cart != null ) {
            OrderEntity newOrder = new OrderEntity();

            newOrder.setOrderDate(LocalDateTime.now());
            newOrder.setTotalAmount(0.0);

            for(ProductEntity product : cart.getProducts()){
                newOrder.addToTotalAmount(product.getPrice());
                //confirm stock
                if(product instanceof BookEntity book) {
                    if (book.getCopies() < 1) {
                        //no stock lets exit, without saying anything, very confusing!
                        return "redirect:/cart";
                    }
                    book.setCopies(book.getCopies() - 1);
                }
                //link the order to what was purchased
                newOrder.addProduct(product);
            }
            //clear the cart
            cart.getProducts().clear();
            //save the parts at the end only
            orderRepository.save(newOrder);
            cartRepository.save(cart);
            //save the stock at the very end so we don't have stock drift on fail
            productRepository.saveAll(newOrder.getProducts());
        }
        return "redirect:/order";
    }
}
