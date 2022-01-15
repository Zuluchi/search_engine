package application.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")

class DefaultControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DefaultController defaultController;

    @Test
    @WithUserDetails("admin")
    public void testIndex() throws Exception {
        File login = new ClassPathResource("templates/index.html").getFile();
        String html = new String(Files.readAllBytes(login.toPath()));
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(html));
    }

    @Test
    public void testLoginSucces() throws Exception{
        mockMvc.perform(formLogin().user("admin").password("admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    public void testLoginError() throws Exception{
        mockMvc.perform(post("/login").param("user","user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithUserDetails("admin")
    public void testLogout() throws Exception{
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}