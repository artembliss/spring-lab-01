package kz.iitu.springlab.catalog;

import java.util.List;
import java.util.Optional;
import kz.iitu.springlab.web.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = "author")
    List<Book> findAll();

    @Override
    @EntityGraph(attributePaths = "author")
    List<Book> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = "author")
    Page<Book> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Book> findById(Long id);

    // 5 derived queries (Lab 8 Task 3.1)
    @EntityGraph(attributePaths = "author")
    List<Book> findByGenre(Genre genre);

    @EntityGraph(attributePaths = "author")
    List<Book> findByTitleContainingIgnoreCase(String fragment);

    @EntityGraph(attributePaths = "author")
    List<Book> findByYearBetweenOrderByYearDesc(int from, int to);

    @EntityGraph(attributePaths = "author")
    List<Book> findTop3ByGenreOrderByYearDesc(Genre genre);

    long countByAuthorName(String name);

    // JPQL query and bulk update (Lab 8 Task 3.2)
    @Query("""
           select b from Book b
           where b.genre = :genre and b.year >= :year
           order by b.title""")
    List<Book> search(@Param("genre") Genre genre, @Param("year") int year);

    @Modifying(clearAutomatically = true)
    @Query("update Book b set b.archived = true where b.year < :year")
    int archiveOlderThan(@Param("year") int year);

    // N+1 fetch join and declarative EntityGraph (Lab 8 Task 3.4)
    @Query("select b from Book b join fetch b.author")
    List<Book> findAllWithAuthor();

    @EntityGraph(attributePaths = "author")
    List<Book> findWithGraphByGenre(Genre genre);

    // Pagination query (Lab 8 Task 4.1)
    @EntityGraph(attributePaths = "author")
    Page<Book> findByGenre(Genre genre, Pageable pageable);

    // Variant 1 query (Lab 8 Variant 1)
    @EntityGraph(attributePaths = {"author", "publisher"})
    List<Book> findByPublisherName(String name);

    // Auxiliary queries for service operations
    boolean existsByTitleIgnoreCase(String title);

    @EntityGraph(attributePaths = "author")
    List<Book> findByAuthorNameContainingIgnoreCase(String authorName);

    @EntityGraph(attributePaths = "author")
    Page<Book> findByAuthorNameContainingIgnoreCase(String authorName, Pageable pageable);
}
