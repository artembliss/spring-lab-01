package kz.iitu.springlab;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void successfulEndpointsAndProxyAreAvailable() throws Exception {
        mockMvc.perform(get("/api/lab4/item/7"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item no. 7"));

        mockMvc.perform(get("/api/lab4/items").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/lab4/proxy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAopProxy").value("true"))
                .andExpect(jsonPath("$.isCglib").value("true"))
                .andExpect(jsonPath("$.className", containsString("SpringCGLIB")));
    }

    @Test
    void invalidRemovalIsNotSuppressed() throws Exception {
        mockMvc.perform(delete("/api/lab4/item/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid identifier: 0"));
    }

    @Test
    void fixedSelfInvocationAndVariantStatisticsWork() throws Exception {
        mockMvc.perform(get("/api/lab4/remove-twice/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Removed item no. 5; Removed item no. 6"));

        mockMvc.perform(get("/api/lab4/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remove").isNumber());
    }
}
