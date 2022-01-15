package application.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")

@WithUserDetails("admin")
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchController searchController;

    @BeforeAll
    private static void setup(@Autowired DataSource dataSource) throws SQLException {
        System.out.println("Скрипты в search");
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_field.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_site.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_page.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_lemma.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_index.sql"));
        }
    }

    @Test
    public void searchTestWithOneWord() throws Exception {
        mockMvc.perform(get("/search")
                        .param("query", "август")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.data[0].site", is("https://www.svetlovka.ru")))
                .andExpect(jsonPath("$.data[0].siteName", is("svetlovka")))
                .andExpect(jsonPath("$.data[0].uri",
                        is("/partners-news/fotovystavka-obektivno-o-moskve/")))
                .andExpect(jsonPath("$.data[0].title").isString())
                .andExpect(jsonPath("$.data[0].snippet").isString())
                .andExpect(jsonPath("$.data[0].relevance", is(1.0)));
    }

    @Test
    public void searchTestWithOneWordAndSiteParam(@Autowired DataSource dataSource) throws Exception {

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_field.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_site.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_page.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_lemma.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_index.sql"));
        }


        mockMvc.perform(get("/search")
                        .param("query", "автор")
                        .param("site", "https://www.svetlovka.ru")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(70)))
                .andExpect(jsonPath("$.data[0].site", is("https://www.svetlovka.ru")))
                .andExpect(jsonPath("$.data[0].siteName", is("svetlovka")))
                .andExpect(jsonPath("$.data[0].uri", startsWith("/projects/")))
                .andExpect(jsonPath("$.data[0].title").isString())
                .andExpect(jsonPath("$.data[0].snippet").isString())
                .andExpect(jsonPath("$.data[0].relevance", is(1.0)));
    }

    @Test
    public void searchTestWithTwoWords() throws Exception {
        mockMvc.perform(get("/search")
                        .param("query", "искусство говорить")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(45)))
                .andExpect(jsonPath("$.data[0].site", is("https://www.svetlovka.ru")))
                .andExpect(jsonPath("$.data[0].siteName", is("svetlovka")))
                .andExpect(jsonPath("$.data[0].uri", is("/books/about/iskusstvo-govorit-publichno/")))
                .andExpect(jsonPath("$.data[0].title").isString())
                .andExpect(jsonPath("$.data[0].snippet").isString())
                .andExpect(jsonPath("$.data[0].relevance", is(1.0)));
    }

    @Test
    public void searchTestWithTwoWordsAndSiteParam(@Autowired DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_field.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_site.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_page.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_lemma.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_index.sql"));
        }

        mockMvc.perform(get("/search")
                        .param("query", "искусство говорить")
                        .param("site", "https://nikoartgallery.com")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(8)))
                .andExpect(jsonPath("$.data[0].site", is("https://nikoartgallery.com")))
                .andExpect(jsonPath("$.data[0].siteName", is("nikoartgallery")))
                .andExpect(jsonPath("$.data[0].uri", is("/stories/all/aleksandr-matveev/")))
                .andExpect(jsonPath("$.data[0].title").isString())
                .andExpect(jsonPath("$.data[0].snippet").isString())
                .andExpect(jsonPath("$.data[0].relevance", is(1.0)));
    }

    @Test
    public void searchTestWithOffset() throws Exception {
        mockMvc.perform(get("/search")
                        .param("query", "август")
                        .param("offset", "5")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.data[0].site", is("https://www.svetlovka.ru")))
                .andExpect(jsonPath("$.data[0].siteName", is("svetlovka")))
                .andExpect(jsonPath("$.data[0].uri",
                        is("/books/collections/istoriya-imperii-i-vospominaniya/")))
                .andExpect(jsonPath("$.data[0].title").isString())
                .andExpect(jsonPath("$.data[0].snippet").isString())
                .andExpect(jsonPath("$.data[0].relevance", is(0.5)));
    }

    @Test
    public void searchTestWithOffsetAndCustomLimit() throws Exception {
        mockMvc.perform(get("/search")
                        .param("query", "август")
                        .param("offset", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    public void searchTestWithEmptyQuery() throws Exception {
        mockMvc.perform(get("/search")
                        .param("query", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("false")))
                .andExpect(jsonPath("$.error", is("Задан пустой поисковый запрос")));
    }
}