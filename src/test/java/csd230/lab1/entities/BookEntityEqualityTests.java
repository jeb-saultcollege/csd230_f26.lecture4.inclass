package csd230.lab1.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BookEntityEqualityTests {

    @Test
    void cartKeepsDifferentNewBooksByTheSameAuthor() {
        BookEntity firstBook = new BookEntity("First Book", 12.99, 2, "Same Author");
        BookEntity secondBook = new BookEntity("Second Book", 15.99, 3, "Same Author");
        CartEntity cart = new CartEntity();

        cart.addProduct(firstBook);
        cart.addProduct(secondBook);

        assertNotEquals(firstBook, secondBook);
        assertEquals(2, cart.getProducts().size());
    }

    @Test
    void savedBooksUseIdForEqualityAndHashCode() {
        BookEntity first = new BookEntity("First Book", 12.99, 2, "Same Author");
        BookEntity samePersistentBook = new BookEntity("Renamed Book", 18.99, 4, "Different Author");
        BookEntity differentBook = new BookEntity("Second Book", 15.99, 3, "Same Author");
        first.setId(7L);
        samePersistentBook.setId(7L);
        differentBook.setId(8L);

        assertEquals(first, samePersistentBook);
        assertEquals(first.hashCode(), samePersistentBook.hashCode());
        assertNotEquals(first, differentBook);
    }
}
