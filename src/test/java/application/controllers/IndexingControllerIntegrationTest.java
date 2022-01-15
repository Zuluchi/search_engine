package application.controllers;

import application.services.SiteService;
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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@WithUserDetails("admin")
class IndexingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndexingController indexingController;

    @Autowired
    private SiteService siteService;

    @BeforeAll
    private static void setup(@Autowired DataSource dataSource) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_field.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_site.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_page.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_lemma.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("dump/search_engine_index.sql"));
        }
    }

    @Test
    public void indexingStartSuccess() throws Exception {
        mockMvc.perform(get("/startIndexing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")));
        assertTrue(siteService.isIndexingStarted());
        siteService.setIndexingStopFlag(true);
    }

    @Test
    public void indexingStartError() throws Exception {
        siteService.setIndexingStarted(true);
        mockMvc.perform(get("/startIndexing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("false")))
                .andExpect(jsonPath("$.error", is("Индексация уже запущена")));
        siteService.setIndexingStarted(false);
    }

    @Test
    public void indexPageSuccess() throws Exception {
        mockMvc.perform(post("/indexPage").param("url", "https://nikoartgallery.com/news/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")));
    }

    @Test
    public void indexPageErrorWhileWrongSite() throws Exception {
        mockMvc.perform(post("/indexPage").param("url", "https://somesite.com/news/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("false")))
                .andExpect(jsonPath("$.error", is("Данная страница находится за пределами сайтов, " +
                        "указаных в конфигурационном файле.")));
    }

    @Test
    public void indexPageErrorWhileIndexingStarted() throws Exception {
        siteService.setIndexingStarted(true);
        mockMvc.perform(post("/indexPage").param("url", "https://somesite.com/news/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("false")))
                .andExpect(jsonPath("$.error", is("Индексация уже запущена. Остановите индексацию, " +
                        "или дождитесь ее окончания")));
        siteService.setIndexingStarted(false);
    }

    @Test
    public void stopIndexingSuccess() throws Exception {
        siteService.setIndexingStarted(true);
        mockMvc.perform(get("/stopIndexing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")));
        assertFalse(siteService.isIndexingStarted());
        assertTrue(siteService.isIndexingStopFlag());
        siteService.setIndexingStarted(false);
    }

    @Test
    public void stopIndexingError() throws Exception {
        mockMvc.perform(get("/stopIndexing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("false")))
                .andExpect(jsonPath("$.error", is("Индексация не запущена")));
        assertFalse(siteService.isIndexingStarted());
        assertFalse(siteService.isIndexingStarted());
    }
}