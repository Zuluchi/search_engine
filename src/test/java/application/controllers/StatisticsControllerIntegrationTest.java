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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@WithUserDetails("admin")
class StatisticsControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatisticsController statisticsController;

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
    public void testGetStatisticsSuccess() throws Exception {
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("true")))
                .andExpect(jsonPath("$.statistics.total.sites").isNumber())
                .andExpect(jsonPath("$.statistics.total.pages").isNumber())
                .andExpect(jsonPath("$.statistics.total.lemmas").isNumber())
                .andExpect(jsonPath("$.statistics.total.indexing").isBoolean())
                .andExpect(jsonPath("$.statistics.detailed[0].url").isString())
                .andExpect(jsonPath("$.statistics.detailed[0].name").isString())
                .andExpect(jsonPath("$.statistics.detailed[0].status").isString())
                .andExpect(jsonPath("$.statistics.detailed[0].statusTime").isString())
                .andExpect(jsonPath("$.statistics.detailed[0].error").hasJsonPath())
                .andExpect(jsonPath("$.statistics.detailed[0].pages").isNumber())
                .andExpect(jsonPath("$.statistics.detailed[0].lemmas").isNumber());
    }
}