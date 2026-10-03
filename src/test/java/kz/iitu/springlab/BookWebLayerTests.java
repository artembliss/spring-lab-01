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

        mockMvc.perform(get("/api/books/999")).andExpect(status().isNotFound());

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
                        .param("year", "2016"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("message"));
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
