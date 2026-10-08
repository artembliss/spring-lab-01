package kz.iitu.springlab;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookWebLayerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void restCrudUsesExpectedStatusCodes() throws Exception {
        mockMvc.perform(get("/api/books").param("author", "Bloch").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Effective Java"));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book with id 999 was not found"));

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Pro Spring 6\",\"author\":\"Cosmina\",\"year\":2023}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/books/")));

        mockMvc.perform(put("/api/books/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Effective Java, 3rd Edition\",\"author\":\"Joshua Bloch\",\"year\":2018}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(delete("/api/books/2")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/books/2")).andExpect(status().isNotFound());
    }

    @Test
    void pageFilterAndPostRedirectGetWork() throws Exception {
        mockMvc.perform(get("/books").param("author", "Walls"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(content().string(containsString("Spring in Action")));

        mockMvc.perform(post("/books")
                        .param("title", "Java Precisely")
                        .param("author", "Sestoft")
                        .param("year", "2016")
                        .param("genre", "TECHNOLOGY")
                        .param("isbn", "978-0-262-03384-8")
                        .param("available", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void formRejectsRequiredFieldsRangeAndCustomConstraints() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("form", "genres"))
                .andExpect(content().string(containsString("New book")));

        mockMvc.perform(post("/books")
                        .param("title", "")
                        .param("author", "Bloch 2018")
                        .param("year", "1200")
                        .param("genre", "")
                        .param("isbn", "978-0-13-468599-2"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("form", "title", "author", "year", "genre", "isbn"))
                .andExpect(content().string(containsString("The title must not be empty")))
                .andExpect(content().string(containsString("The year must be between 1450 and 2100")))
                .andExpect(content().string(containsString("Choose a genre")))
                .andExpect(content().string(containsString("The author must not contain digits")))
                .andExpect(content().string(containsString("Enter a valid ISBN-10 or ISBN-13 checksum")))
                .andExpect(content().string(containsString("Bloch 2018")));
    }

    @Test
    void localeSwitchesCaptionsAndValidationMessages() throws Exception {
        mockMvc.perform(post("/books")
                        .param("lang", "ru")
                        .param("title", "")
                        .param("author", "Автор")
                        .param("year", "2020")
                        .param("genre", "FICTION"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Новая книга")))
                .andExpect(content().string(containsString("Название не может быть пустым")));
    }

    @Test
    void missingBookUsesSeparatePageAndApiAdvice() throws Exception {
        mockMvc.perform(get("/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/not-found"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Book with id 999 was not found")));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void mappingFailuresProduceRequiredErrorStatuses() throws Exception {
        mockMvc.perform(get("/api/books/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/nothing")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/books")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/books").contentType(MediaType.TEXT_PLAIN).content("not json"))
                .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(get("/api/books").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void variantOneSearchIsCaseInsensitiveAndEmptyIsStillOk() throws Exception {
        mockMvc.perform(get("/api/books/search").param("title", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring in Action"));
        mockMvc.perform(get("/api/books/search").param("title", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
