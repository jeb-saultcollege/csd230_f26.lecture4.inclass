package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.repositories.BookEntityRepository;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Book REST API", description = "JSON API for managing books")
@RestController
@RequestMapping("/api/rest/books")
@CrossOrigin(origins = "*")
public class BookRestController {
    private final BookEntityRepository repository;

    public BookRestController(BookEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List books", description = "Returns all books")
    @ApiResponse(responseCode = "200", description = "Books returned")
    public List<BookEntity> all() {
        return repository.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a book", description = "Creates a book from the supplied JSON")
    @ApiResponse(responseCode = "200", description = "Book created")
    public BookEntity newBook(@RequestBody BookEntity newBook) {
        return repository.save(newBook);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book", description = "Returns a book by ID")
    @ApiResponse(responseCode = "200", description = "Book returned")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public BookEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a book", description = "Replaces an existing book")
    @ApiResponse(responseCode = "200", description = "Book replaced")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public BookEntity replaceBook(@RequestBody BookEntity newBook, @PathVariable Long id) {
        return repository.findById(id)
                .map(book -> {
                    book.setTitle(newBook.getTitle());
                    book.setAuthor(newBook.getAuthor());
                    book.setPrice(newBook.getPrice());
                    book.setCopies(newBook.getCopies());
                    book.setIsbn(newBook.getIsbn());
                    return repository.save(book);
                })
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes the book with the supplied ID")
    @ApiResponse(responseCode = "200", description = "Book deleted")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public void deleteBook(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
