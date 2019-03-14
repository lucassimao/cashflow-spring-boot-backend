package com.lucassimao;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FluxoDeCaixaApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackages={"com.lucassimao.fluxodecaixa.config","com.lucassimao"})
public class SecurityTests{

	@Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EntityManager entityManager;


    @Before
    public void setup() throws Exception {
        long idUser = testUtils.criarUsuario("Lucas Simao", "papai@noel.com", "1234637463746$%$#$$");
        String tokenUsuario = testUtils.doLogin("papai@noel.com", "1234637463746$%$#$$");
        testUtils.setupBookEntryGroupAndBookEntry("bookEntryGroupDescription", BookEntryType.Expense, idUser, tokenUsuario, "bookEntryDescription");
    }

    @Test
    public void requisicoesNaoAutenticadasNaoPodemUtilizarAPI() throws Exception {
        BookEntry bookEntry = entityManager.createQuery("from BookEntry",BookEntry.class).getSingleResult();
        assertNotNull(bookEntry);
        assertNotNull(bookEntry.getBookEntryGroup());

        long idBookEntry = bookEntry.getId(), idBookEntryGroup = bookEntry.getBookEntryGroup().getId(); 

        mvc.perform(get("/bookEntryGroups")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
            
        mvc.perform(post("/bookEntryGroups")
            .content(mapper.writeValueAsString(new BookEntryGroup()))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        mvc.perform(patch("/bookEntryGroups/" +idBookEntryGroup)
            .content(mapper.writeValueAsString(Map.of("description", "edited by anonymous user")))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        mvc.perform(delete("/bookEntryGroups/" +idBookEntryGroup))
            .andExpect(status().isForbidden());              

        mvc.perform(get("/bookEntries")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());   
            
        mvc.perform(post("/bookEntries")
            .content(mapper.writeValueAsString(new BookEntry()))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
        
            
        mvc.perform(patch("/bookEntries/" + idBookEntry)
            .content(mapper.writeValueAsString(Map.of("description", "edited by anonymous user")))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        mvc.perform(delete("/bookEntries/" + idBookEntry))
            .andExpect(status().isForbidden());            

    }
}