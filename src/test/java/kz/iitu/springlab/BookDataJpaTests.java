package kz.iitu.springlab;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kz.iitu.springlab.catalog.Author;
import kz.iitu.springlab.catalog.AuthorRepository;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookRepository;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.catalog.Publisher;
import kz.iitu.springlab.catalog.PublisherRepository;
import kz.iitu.springlab.catalog.Tag;
import kz.iitu.springlab.catalog.TagRepository;
import kz.iitu.springlab.web.Genre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class BookDataJpaTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Task 1 & 2: Database entities and associations are seeded properly")
    @Transactional
    void databaseEntitiesAndAssociationsAreSeeded() {
        assertThat(authorRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(tagRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(bookRepository.count()).isGreaterThanOrEqualTo(8);
        assertThat(publisherRepository.count()).isGreaterThanOrEqualTo(3);

        Author bloch = authorRepository.findByName("Joshua Bloch").orElseThrow();
        assertThat(bloch.getBooks()).hasSizeGreaterThanOrEqualTo(3);

        Book effectiveJava = bookRepository.findByTitleContainingIgnoreCase("Effective Java")
                .stream()
                .filter(b -> b.getTitle().equals("Effective Java"))
                .findFirst()
                .orElseThrow();

        assertThat(effectiveJava.getAuthor().getName()).isEqualTo("Joshua Bloch");
        assertThat(effectiveJava.getTags()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(effectiveJava.getPublisher()).isNotNull();
        assertThat(effectiveJava.getPublisher().getName()).isEqualTo("Addison-Wesley");
    }

    @Test
    @DisplayName("Task 3.1: Five derived queries return non-empty, expected results")
    void fiveDerivedQueriesReturnExpectedResults() {
        // 1. findByGenre
        List<Book> techBooks = bookRepository.findByGenre(Genre.TECHNOLOGY);
        assertThat(techBooks).isNotEmpty();

        // 2. findByTitleContainingIgnoreCase
        List<Book> actionBooks = bookRepository.findByTitleContainingIgnoreCase("action");
        assertThat(actionBooks).hasSize(2);

        // 3. findByYearBetweenOrderByYearDesc
        List<Book> between = bookRepository.findByYearBetweenOrderByYearDesc(2005, 2018);
        assertThat(between).isNotEmpty();
        assertThat(between.get(0).getYear()).isGreaterThanOrEqualTo(between.get(between.size() - 1).getYear());

        // 4. findTop3ByGenreOrderByYearDesc
        List<Book> top3 = bookRepository.findTop3ByGenreOrderByYearDesc(Genre.TECHNOLOGY);
        assertThat(top3).hasSize(3);
        assertThat(top3.get(0).getYear()).isGreaterThanOrEqualTo(top3.get(1).getYear());

        // 5. countByAuthorName
        long blochCount = bookRepository.countByAuthorName("Joshua Bloch");
        assertThat(blochCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Task 3.2: JPQL query with named parameters and bulk update")
    @Transactional
    void jpqlQueryAndBulkUpdateWork() {
        // JPQL query
        List<Book> recentTech = bookRepository.search(Genre.TECHNOLOGY, 2015);
        assertThat(recentTech).isNotEmpty();
        for (Book b : recentTech) {
            assertThat(b.getGenre()).isEqualTo(Genre.TECHNOLOGY);
            assertThat(b.getYear()).isGreaterThanOrEqualTo(2015);
        }

        // Bulk update
        int archivedCount = bookService.archiveOlderThan(2010);
        assertThat(archivedCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Task 3.4: Fetch join resolves N+1 query problem")
    void nPlusOneFetchJoinResolvesAssociations() {
        List<Book> booksWithAuthor = bookRepository.findAllWithAuthor();
        assertThat(booksWithAuthor).isNotEmpty();
        for (Book book : booksWithAuthor) {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getName()).isNotBlank();
        }

        List<Book> graphBooks = bookRepository.findWithGraphByGenre(Genre.TECHNOLOGY);
        assertThat(graphBooks).isNotEmpty();
    }

    @Test
    @DisplayName("Task 4: Paging and sorting returns correct page, slice and counts")
    void paginationAndSortingWorkCorrectly() {
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "year"));
        Page<Book> page = bookRepository.findAll(pageRequest);

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(8);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(3);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Variant 1: Publisher entity with @OneToMany and derived query")
    @Transactional
    void variantOnePublisherEntityAndDerivedQuery() {
        List<Book> addisonBooks = bookRepository.findByPublisherName("Addison-Wesley");
        assertThat(addisonBooks).hasSize(3);
        for (Book book : addisonBooks) {
            assertThat(book.getPublisher().getName()).isEqualTo("Addison-Wesley");
        }

        List<Book> manningBooks = bookRepository.findByPublisherName("Manning");
        assertThat(manningBooks).hasSize(2);
        for (Book book : manningBooks) {
            assertThat(book.getPublisher().getName()).isEqualTo("Manning");
        }
    }
}
