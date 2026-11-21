package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Books", description = "Operations related to Book Management")
@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookEntityRepository bookRepository;
    @Autowired
    private CartEntityRepository cartRepository;
    @Operation(summary = "Get all books", description = "Returns the HTML view of the book list")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "bookList"; // Looks for bookList.html in templates
    }
    @Operation(summary = "Get book by ID", description = "Returns the HTML view of a single book's details")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @GetMapping("/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "bookDetails";
    }
    @Operation(summary = "Show add book form", description = "Returns the HTML form to create a new book")
    @ApiResponse(responseCode = "200", description = "Successfully returned form")
    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new BookEntity());
        return "addBook";
    }
    @Operation(summary = "Add a new book", description = "Saves a new book and redirects to the book list")
    @ApiResponse(responseCode = "302", description = "Book created, redirected to book list")
    @PostMapping("/add")
    public String addBook(@ModelAttribute BookEntity book) {
        bookRepository.save(book);
        return "redirect:/books";
    }
    @Operation(summary = "Show edit book form", description = "Returns the HTML form to edit an existing book")
    @ApiResponse(responseCode = "200", description = "Successfully returned form")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "editBook";
    }
    @Operation(summary = "Edit an existing book", description = "Updates the book and redirects to the book list")
    @ApiResponse(responseCode = "302", description = "Book updated, redirected to book list")
    @PostMapping("/edit/{id}")
    public String editBook(@PathVariable Long id, @ModelAttribute BookEntity updatedBook) {
        BookEntity existingBook = bookRepository.findById(id).orElse(null);
        if (existingBook != null) {
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setPrice(updatedBook.getPrice());
            existingBook.setCopies(updatedBook.getCopies());
            bookRepository.save(existingBook);
        }
        return "redirect:/books";
    }
    @Operation(summary = "Delete a book", description = "Deletes the book (and removes it from any carts) and redirects to the book list")
    @ApiResponse(responseCode = "302", description = "Book deleted or not found, redirected to book list")
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            // Remove this book from any Carts that contain it before deleting
            for (CartEntity cart : book.getCarts()) {
                cart.getProducts().remove(book);
                cartRepository.save(cart);
            }
            bookRepository.deleteById(id);
        }
        return "redirect:/books";
    }
}
