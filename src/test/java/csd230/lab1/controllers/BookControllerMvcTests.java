package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import csd230.lab1.repositories.UserEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
class BookControllerMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookEntityRepository bookRepository;

    @MockitoBean
    private CartEntityRepository cartRepository;

    @MockitoBean
    private ProductEntityRepository productRepository;

    @MockitoBean
    private UserEntityRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    csd230.lab1.auth.JwtUtil jwtUtil;

    @Test
    void listBooksRendersBookListWithBooks() throws Exception {
        BookEntity book = new BookEntity("Spring MVC", 29.99, 5, "Course Example");
        when(bookRepository.findAll()).thenReturn(java.util.List.of(book));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("bookList"))
                .andExpect(model().attributeExists("books"));

        verify(bookRepository).findAll();
    }

    @Test
    void showBookRendersBookDetails() throws Exception {
        BookEntity book = new BookEntity("Spring MVC", 29.99, 5, "Course Example");
        book.setId(42L);
        when(bookRepository.findById(42L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("bookDetails"))
                .andExpect(model().attribute("book", book));
    }

    @Test
    void addBookFormRendersEmptyBookForm() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("addBook"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void addBookSavesBookAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/books/add")
                        .param("title", "Spring MVC")
                        .param("author", "Course Example")
                        .param("price", "29.99")
                        .param("copies", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookRepository).save(any(BookEntity.class));
    }

    @Test
    void editBookFormRendersExistingBook() throws Exception {
        BookEntity book = new BookEntity("Spring MVC", 29.99, 5, "Course Example");
        book.setId(42L);
        when(bookRepository.findById(42L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/edit/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("editBook"))
                .andExpect(model().attribute("book", book));
    }

    @Test
    void editBookUpdatesBookAndRedirectsToList() throws Exception {
        BookEntity existingBook = new BookEntity("Old Title", 10.0, 1, "Old Author");
        existingBook.setId(42L);
        when(bookRepository.findById(42L)).thenReturn(Optional.of(existingBook));

        mockMvc.perform(post("/books/edit/42")
                        .param("title", "New Title")
                        .param("author", "New Author")
                        .param("price", "39.99")
                        .param("copies", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookRepository).save(existingBook);
    }

    @Test
    void deleteBookRemovesItFromCartsBeforeDeleting() throws Exception {
        BookEntity book = new BookEntity("Spring MVC", 29.99, 5, "Course Example");
        book.setId(42L);
        CartEntity cart = new CartEntity();
        cart.setId(7L);
        cart.addProduct(book);
        when(bookRepository.findById(42L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/delete/42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(cartRepository).save(cart);
        verify(bookRepository).deleteById(42L);
    }

    @Test
    void deleteMissingBookOnlyRedirectsToList() throws Exception {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/delete/42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeCount(0));

        verify(bookRepository, never()).deleteById(42L);
        verify(cartRepository, never()).save(any(CartEntity.class));
    }
}
