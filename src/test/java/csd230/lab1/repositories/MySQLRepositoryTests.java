package csd230.lab1.repositories;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Lecture 2.1: run against the real MySQL container (Docker, port 3307), NOT the embedded H2
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySQLRepositoryTests.TestPasswordEncoderConfig.class)
class MySQLRepositoryTests {

    @TestConfiguration(proxyBeanMethods = false)
    static class TestPasswordEncoderConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookEntityRepository bookEntityRepository;

    @Autowired
    private CartEntityRepository cartEntityRepository;

    @Autowired
    private ProductEntityRepository productEntityRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // 1. CRUD test - @DataJpaTest rolls back automatically, so MySQL stays clean
    @Test
    void crudTest() {
        // CREATE
        BookEntity book = new BookEntity("Clean Code", 45.99, 10, "Robert C. Martin");
        book.setIsbn("9780132350884");
        BookEntity saved = bookEntityRepository.save(book);
        entityManager.flush();
        assertNotNull(saved.getId(), "Saved book should get an identity id from MySQL");

        // READ
        BookEntity found = bookEntityRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Clean Code", found.getTitle());
        assertEquals("Robert C. Martin", found.getAuthor());
        assertEquals(45.99, found.getPrice());

        // UPDATE
        found.setPrice(39.99);
        found.setCopies(7);
        BookEntity updated = bookEntityRepository.save(found);
        entityManager.flush();
        assertEquals(39.99, updated.getPrice());
        assertEquals(7, updated.getCopies());
        assertEquals(39.99, bookEntityRepository.findById(saved.getId()).orElseThrow().getPrice());

        // DELETE
        bookEntityRepository.delete(updated);
        entityManager.flush();
        assertTrue(bookEntityRepository.findById(saved.getId()).isEmpty());
    }

    // 2. Derived-query test - rolled back automatically after the test
    @Test
    void derivedQueryTest() {
        // Use unique values so the test is independent of any pre-existing rows
        String uniqueAuthor = "UniqueAuthor_" + System.nanoTime();
        String uniqueIsbn = "isbn-" + System.nanoTime();
        BookEntity b1 = new BookEntity("JUnit in Action", 30.0, 5, uniqueAuthor);
        b1.setIsbn(uniqueIsbn);
        BookEntity b2 = new BookEntity("Spring in Action", 40.0, 3, "Craig Walls");
        b2.setIsbn("isbn-spring-" + System.nanoTime());
        bookEntityRepository.save(b1);
        bookEntityRepository.save(b2);
        entityManager.flush();
        entityManager.clear();

        List<BookEntity> byAuthor = bookEntityRepository.findByAuthor(uniqueAuthor);
        assertEquals(1, byAuthor.size());
        assertEquals("JUnit in Action", byAuthor.get(0).getTitle());

        List<BookEntity> byIsbn = bookEntityRepository.findByIsbn(uniqueIsbn);
        assertEquals(1, byIsbn.size());
        assertEquals("JUnit in Action", byIsbn.get(0).getTitle());

        // Filter by the unique title so pre-existing faker books don't interfere
        List<BookEntity> byPrice = bookEntityRepository.findByPriceBetween(25.0, 35.0).stream()
                .filter(b -> "JUnit in Action".equals(b.getTitle()))
                .toList();
        assertEquals(1, byPrice.size());
        assertEquals("JUnit in Action", byPrice.get(0).getTitle());
    }

    // 3. Cart <-> Product @ManyToMany relationship-persistence test.
    // NOT_SUPPORTED suspends the test transaction so all writes commit to MySQL and
    // remain inspectable; the Cart+Product graph is persisted inside one TransactionTemplate
    // (a separate repository save would treat the committed products as detached).
    // Rows are cleaned up at the end unless the test fails.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cartProductRelationshipPersistenceTest() {
        // Persist the cart + products in a single committed transaction
        CartEntity savedCart = transactionTemplate.execute(status -> {
            BookEntity book1 = new BookEntity("Design Patterns", 55.0, 4, "Gang of Four");
            BookEntity book2 = new BookEntity("Refactoring", 50.0, 6, "Martin Fowler");
            CartEntity cart = new CartEntity();
            cart.addProduct(book1);
            cart.addProduct(book2);
            cartEntityRepository.save(cart);
            return cart;
        });
        assertNotNull(savedCart);
        assertNotNull(savedCart.getId());

        // Re-load cart and product (instances from the first commit are detached),
        // re-add the duplicate product, and confirm no second join-table row appears
        // (LinkedHashSet). Runs in its own committed transaction so lazy collections
        // stay resolvable and no detached/managed mix reaches merge.
        Long cartId = savedCart.getId();
        Long firstProductId = savedCart.getProducts().iterator().next().getId();
        transactionTemplate.executeWithoutResult(status -> {
            CartEntity managedCart = cartEntityRepository.findById(cartId).orElseThrow();
            BookEntity reloadedBook = bookEntityRepository.findById(firstProductId).orElseThrow();
            managedCart.addProduct(reloadedBook);
            cartEntityRepository.saveAndFlush(managedCart);
        });

        // Verify the join table directly in MySQL: exactly one row per cart<->product pair
        Integer links = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_products WHERE cart_id = ?",
                Integer.class, savedCart.getId());
        assertEquals(2, links, "cart_products should hold exactly 2 rows (no duplicates)");

        Integer cartRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_entity WHERE id = ?",
                Integer.class, savedCart.getId());
        assertEquals(1, cartRows);

        // Owning side cleanup: deleting the cart removes its join-table rows
        cartEntityRepository.delete(savedCart);
        Integer linksAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_products WHERE cart_id = ?",
                Integer.class, savedCart.getId());
        assertEquals(0, linksAfter, "Join-table rows must be removed with the cart");

        // Remove the two products committed earlier
        for (ProductEntity p : savedCart.getProducts()) {
            productEntityRepository.deleteById(p.getId());
        }
    }

    // 4. Characterization test: MySQL must reject deletion of a product that is
    // still referenced by cart_products. The transaction is intentionally expected
    // to fail; the test passes only when that foreign-key protection is observed.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deletingBookReferencedByCartFailsWithForeignKeyConstraint() {
        Long[] ids = transactionTemplate.execute(status -> {
            BookEntity book = new BookEntity("Referenced Book", 25.0, 1, "Test Author");
            book.setIsbn("fk-" + System.nanoTime());

            CartEntity cart = new CartEntity();
            cart.addProduct(book);
            CartEntity savedCart = cartEntityRepository.saveAndFlush(cart);

            Long bookId = savedCart.getProducts().iterator().next().getId();
            return new Long[]{savedCart.getId(), bookId};
        });
        assertNotNull(ids);

        try {
            DataIntegrityViolationException exception = assertThrows(
                    DataIntegrityViolationException.class,
                    () -> transactionTemplate.executeWithoutResult(status -> {
                        BookEntity referencedBook = bookEntityRepository.findById(ids[1]).orElseThrow();
                        bookEntityRepository.delete(referencedBook);
                        bookEntityRepository.flush();
                    }),
                    "MySQL must reject deleting a book that cart_products still references"
            );

            boolean mysqlConstraintCauseFound = false;
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    mysqlConstraintCauseFound = true;
                    break;
                }
            }
            assertTrue(mysqlConstraintCauseFound,
                    "The underlying MySQL SQLIntegrityConstraintViolationException should be retained");
        } finally {
            // Clean up in the safe order: remove the owning Cart first, then its Book.
            transactionTemplate.executeWithoutResult(status -> {
                CartEntity cart = cartEntityRepository.findById(ids[0]).orElseThrow();
                cartEntityRepository.delete(cart);
                cartEntityRepository.flush();
                productEntityRepository.deleteById(ids[1]);
                productEntityRepository.flush();
            });
        }
    }
}
