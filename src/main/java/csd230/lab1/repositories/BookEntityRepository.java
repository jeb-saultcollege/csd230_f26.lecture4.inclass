package csd230.lab1.repositories;

import csd230.lab1.entities.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookEntityRepository extends JpaRepository<BookEntity, Long>{
    List<BookEntity> findByIsbn(String isbn);
    // JpaRepository already provides Optional<BookEntity> findById(Long id).

    List<BookEntity> findByTitle(String title);

    List<BookEntity> findByTitleLike(String title);

    List<BookEntity> findByAuthor(String author);

    List<BookEntity> findByPriceBetween(double minPrice, double maxPrice);

    @Query("SELECT b FROM BookEntity b WHERE b.price >= :minPrice AND b.price <= :maxPrice")
    List<BookEntity> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
}