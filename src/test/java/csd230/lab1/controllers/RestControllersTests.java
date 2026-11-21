package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.DiscMagEntity;
import csd230.lab1.entities.MagazineEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.entities.TicketEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.DiscMagEntityRepository;
import csd230.lab1.repositories.MagazineEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import csd230.lab1.repositories.TicketEntityRepository;
import csd230.lab1.repositories.UserEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BookRestController.class,
        MagazineRestController.class,
        DiscMagRestController.class,
        TicketRestController.class,
        CartRestController.class,
        OrderRestController.class
})
@AutoConfigureMockMvc(addFilters = false)
class RestControllersTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookEntityRepository bookRepository;
    @MockitoBean
    private MagazineEntityRepository magazineRepository;
    @MockitoBean
    private DiscMagEntityRepository discMagRepository;
    @MockitoBean
    private TicketEntityRepository ticketRepository;
    @MockitoBean
    private CartEntityRepository cartRepository;
    @MockitoBean
    private OrderEntityRepository orderRepository;
    @MockitoBean
    private ProductEntityRepository productRepository;
    @MockitoBean
    private UserEntityRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    csd230.lab1.auth.JwtUtil jwtUtil;

    @Test
    void booksCanBeListedAndCreated() throws Exception {
        BookEntity book = new BookEntity("Test Book", 14.99, 3, "Test Author");
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookRepository.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
        mockMvc.perform(post("/api/rest/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New Book","author":"Test Author","price":19.99,"copies":4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Book"));

        verify(bookRepository).save(any(BookEntity.class));
    }

    @Test
    void putBookReplacesIsbnAndOtherFields() throws Exception {
        BookEntity existingBook = new BookEntity("Old Title", 10.0, 1, "Same Author");
        existingBook.setId(42L);
        existingBook.setIsbn("old-isbn");
        when(bookRepository.findById(42L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/rest/books/42").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New Title","author":"Same Author","price":20.50,"copies":5,"isbn":"new-isbn"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.isbn").value("new-isbn"));

        verify(bookRepository).save(existingBook);
    }

    @Test
    void putBookReturns404WhenIdDoesNotExist() throws Exception {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/rest/books/999").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Missing Book","author":"Test Author","price":12.99,"copies":2,"isbn":"missing-isbn"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Could not find book with ID: 999"));

        verify(bookRepository, never()).save(any(BookEntity.class));
    }

    @Test
    void magazinesCanBeListedAndCreated() throws Exception {
        MagazineEntity magazine = new MagazineEntity("Test Magazine", 5.99, 10, 4,
                LocalDateTime.of(2026, 10, 1, 0, 0));
        when(magazineRepository.findAll()).thenReturn(List.of(magazine));
        when(magazineRepository.save(any(MagazineEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/magazines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Magazine"));
        mockMvc.perform(post("/api/rest/magazines").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New Magazine","price":6.99,"copies":12,"orderQty":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderQty").value(5));

        verify(magazineRepository).save(any(MagazineEntity.class));
    }

    @Test
    void discMagsCanBeListedAndCreated() throws Exception {
        DiscMagEntity discMag = new DiscMagEntity("Test DiscMag", 8.99, 8, 3,
                LocalDateTime.of(2026, 10, 1, 0, 0), true);
        when(discMagRepository.findAll()).thenReturn(List.of(discMag));
        when(discMagRepository.save(any(DiscMagEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/discmags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hasDisc").value(true));
        mockMvc.perform(post("/api/rest/discmags").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New DiscMag","price":9.99,"copies":6,"orderQty":2,"hasDisc":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New DiscMag"));

        verify(discMagRepository).save(any(DiscMagEntity.class));
    }

    @Test
    void ticketsCanBeListedAndCreated() throws Exception {
        TicketEntity ticket = new TicketEntity("Test Event", 24.50);
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Event"));
        mockMvc.perform(post("/api/rest/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"New Event","price":29.50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(29.50));

        verify(ticketRepository).save(any(TicketEntity.class));
    }

    @Test
    void cartsCanBeListedAndCreated() throws Exception {
        CartEntity cart = new CartEntity();
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].products").isArray());
        mockMvc.perform(post("/api/rest/carts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"products\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());

        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void cartWithBidirectionalBookReturnsProductWithoutInverseLinks() throws Exception {
        BookEntity book = new BookEntity("Linked Book", 14.99, 3, "Test Author");
        book.setId(42L);
        book.setIsbn("test-isbn");
        CartEntity cart = new CartEntity();
        cart.setId(7L);
        cart.addProduct(book);
        OrderEntity order = new OrderEntity();
        order.setId(8L);
        order.setTotalAmount(14.99);
        order.addProduct(book);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(cart));

        mockMvc.perform(get("/api/rest/carts/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].id").value(42))
                .andExpect(jsonPath("$.products[0].title").value("Linked Book"))
                .andExpect(jsonPath("$.products[0].author").value("Test Author"))
                .andExpect(jsonPath("$.products[0].isbn").value("test-isbn"))
                .andExpect(jsonPath("$.products[0].price").value(14.99))
                .andExpect(jsonPath("$.products[0].copies").value(3))
                .andExpect(jsonPath("$.products[0].carts").doesNotExist())
                .andExpect(jsonPath("$.products[0].orders").doesNotExist());
    }

    @Test
    void orderWithBidirectionalBookReturnsProductWithoutInverseLinks() throws Exception {
        BookEntity book = new BookEntity("Linked Book", 14.99, 3, "Test Author");
        book.setId(42L);
        book.setIsbn("test-isbn");
        CartEntity cart = new CartEntity();
        cart.setId(7L);
        cart.addProduct(book);
        OrderEntity order = new OrderEntity();
        order.setId(8L);
        order.setTotalAmount(14.99);
        order.addProduct(book);
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/rest/orders/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.totalAmount").value(14.99))
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].id").value(42))
                .andExpect(jsonPath("$.products[0].title").value("Linked Book"))
                .andExpect(jsonPath("$.products[0].author").value("Test Author"))
                .andExpect(jsonPath("$.products[0].isbn").value("test-isbn"))
                .andExpect(jsonPath("$.products[0].price").value(14.99))
                .andExpect(jsonPath("$.products[0].copies").value(3))
                .andExpect(jsonPath("$.products[0].carts").doesNotExist())
                .andExpect(jsonPath("$.products[0].orders").doesNotExist());
    }

    @Test
    void ordersCanBeListedAndCreated() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setTotalAmount(24.50);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/rest/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(24.50));
        mockMvc.perform(post("/api/rest/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalAmount":31.25,"orderDate":"2026-10-28T12:00:00","products":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(31.25));

        verify(orderRepository).save(any(OrderEntity.class));
    }
}
