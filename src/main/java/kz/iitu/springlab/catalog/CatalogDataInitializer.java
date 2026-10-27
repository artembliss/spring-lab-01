package kz.iitu.springlab.catalog;

import java.util.Set;
import kz.iitu.springlab.web.Genre;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogDataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    public CatalogDataInitializer(
            AuthorRepository authorRepository,
            TagRepository tagRepository,
            PublisherRepository publisherRepository,
            BookRepository bookRepository
    ) {
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        // 4 Tags
        Tag tagJava = tagRepository.save(new Tag("Java"));
        Tag tagArch = tagRepository.save(new Tag("Architecture"));
        Tag tagSpring = tagRepository.save(new Tag("Spring"));
        Tag tagBest = tagRepository.save(new Tag("Best Practices"));

        // Publishers for Variant 1
        Publisher pubAddison = publisherRepository.save(new Publisher("Addison-Wesley"));
        Publisher pubPrentice = publisherRepository.save(new Publisher("Prentice Hall"));
        Publisher pubManning = publisherRepository.save(new Publisher("Manning"));

        // 3 Authors
        Author bloch = new Author("Joshua Bloch", "USA");
        Author martin = new Author("Robert Martin", "USA");
        Author walls = new Author("Craig Walls", "USA");

        // 8 Books
        // Bloch (3 books)
        Book b1 = new Book("Effective Java", Genre.TECHNOLOGY, 2018);
        b1.setTags(Set.of(tagJava, tagBest));
        bloch.addBook(b1);
        pubAddison.addBook(b1);

        Book b2 = new Book("Java Puzzlers", Genre.TECHNOLOGY, 2005);
        b2.setTags(Set.of(tagJava));
        bloch.addBook(b2);
        pubAddison.addBook(b2);

        Book b3 = new Book("Effective Java Programming", Genre.TECHNOLOGY, 2001);
        b3.setTags(Set.of(tagJava));
        bloch.addBook(b3);
        pubAddison.addBook(b3);

        // Martin (3 books)
        Book b4 = new Book("Clean Code", Genre.TECHNOLOGY, 2008);
        b4.setTags(Set.of(tagArch, tagBest));
        martin.addBook(b4);
        pubPrentice.addBook(b4);

        Book b5 = new Book("Clean Architecture", Genre.TECHNOLOGY, 2017);
        b5.setTags(Set.of(tagArch));
        martin.addBook(b5);
        pubPrentice.addBook(b5);

        Book b6 = new Book("The Clean Coder", Genre.TECHNOLOGY, 2011);
        b6.setTags(Set.of(tagBest));
        martin.addBook(b6);
        pubPrentice.addBook(b6);

        // Walls (2 books)
        Book b7 = new Book("Spring in Action", Genre.TECHNOLOGY, 2022);
        b7.setTags(Set.of(tagSpring, tagJava));
        walls.addBook(b7);
        pubManning.addBook(b7);

        Book b8 = new Book("Spring Boot in Action", Genre.TECHNOLOGY, 2016);
        b8.setTags(Set.of(tagSpring));
        walls.addBook(b8);
        pubManning.addBook(b8);

        // Save authors (cascade saves books)
        authorRepository.save(bloch);
        authorRepository.save(martin);
        authorRepository.save(walls);
    }
}
