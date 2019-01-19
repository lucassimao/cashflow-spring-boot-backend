package com.lucassimao.repositories;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.TestUtils;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.MediaTypes;
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
@ComponentScan(basePackageClasses=TestUtils.class)
public class BookEntryRepositoryTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestUtils testUtils;

    private String tokenPrimeiroUsuario, tokenSegundoUsuario;
    private BookEntry bookEntry1, bookEntry2;
   

    /**
     * Antes de cada teste: - cadastra 2 usuários, armazenando o login dos mesmos -
     * realiza o login de cada um e armazena os tokens de autenticação - faz o
     * cadastro de 2 BookEntryGroup, um p/ cada usuario - faz o cadastro de 2
     * BookEntry, um p/ cada usuario e guarda referencia pra cada um
     * 
     * @throws JsonProcessingException
     * @throws Exception
     */
    @Before
    public void setup() throws JsonProcessingException, Exception {
        long idPrimeiroUsuario = testUtils.criarUsuario("Lucas Simao", "papai@noel.com", "1234637463746$%$#$$");
        this.tokenPrimeiroUsuario = testUtils.doLogin("papai@noel.com", "1234637463746$%$#$$");

        long idSegundoUsuario = testUtils.criarUsuario("xpto da silva", "xpto@gmail.com", "123");
        this.tokenSegundoUsuario = testUtils.doLogin("xpto@gmail.com", "123");

        this.bookEntry1 = testUtils.setupBookEntryGroupAndBookEntry("Fatura de Cartão de Crédito",BookEntryType.Expense,idPrimeiroUsuario, tokenPrimeiroUsuario,"Pagamento do cartão de crédito");
        this.bookEntry2 = testUtils.setupBookEntryGroupAndBookEntry("Pagamentos a Receber",BookEntryType.Income,idSegundoUsuario, tokenSegundoUsuario,"Boleto do cliente #12345");
    }



    /**
     * Testando se usuario consegue ler BookEntry cadastrados por outros usuários
     * @throws Exception
     */
    @Test
    public void userCanReadOnlyHisOwnBookEntries() throws Exception {

        // usuario #1 tenta requisitar todos e so recebe os que ele cadastrou
        mvc.perform(get("/bookEntries")
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is(bookEntry1.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntries[0]._links.self.href", endsWith("bookEntries/" + this.bookEntry1.getId())));

       // usuário #1 tentando requisitar BookEntryGroup cadastrado pelo usuário #2 - n deve conseguir
       mvc.perform(get("/bookEntries/" + this.bookEntry2.getId())
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // usuario #2 tenta requisitar todos e so recebe os que ele cadastrou
        mvc.perform(get("/bookEntries")
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is(bookEntry2.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntries[0]._links.self.href", endsWith("bookEntries/" + this.bookEntry2.getId())));

       // usuário #2 tentando requisitar BookEntryGroup cadastrado pelo usuário #1 - n deve conseguir
       mvc.perform(get("/bookEntries/" + this.bookEntry1.getId())
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());            
    }


    /**
     * Testando se usuario consegue editar BookEntries cadastrados por outros usuários
     * @throws Exception
     */
    @Test
    public void userCanEditOnlyHisOwnBookEntryGroups() throws Exception {
   
        // usuario #1 deve conseguir atualizar BookEntry que ele cadastrou
        mvc.perform(patch("/bookEntries/"+ this.bookEntry1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "edited by 1st user")))
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // usuario #1 NÃO deve conseguir atualizar BookEntry que usuario #2 cadastrou
        mvc.perform(patch("/bookEntries/"+ this.bookEntry2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 1st user")))
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());       
            
        // usuario #2 NÃO deve conseguir atualizar BookEntry que usuario #1 cadastrou
        mvc.perform(patch("/bookEntries/"+ this.bookEntry1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 2nd user")))
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // usuario #2 deve conseguir atualizar BookEntry que ele cadastrou
        mvc.perform(patch("/bookEntries/"+ this.bookEntry2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "edited by 2nd user")))
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());                

    }

    @Test
    public void user1CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // usuario #1 deve conseguir excluir BookEntry que ele cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry1.getId())
            .header("Authorization", tokenPrimeiroUsuario))
            .andExpect(status().isNoContent());

        // usuario #1 NÃO deve conseguir excluir BookEntry que usuario #2 cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry2.getId())
            .header("Authorization", tokenPrimeiroUsuario))
            .andExpect(status().isNotFound());   
    }

    @Test
    public void user2CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // usuario #2 deve conseguir excluir BookEntryGroup que ele cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry2.getId())
            .header("Authorization", tokenSegundoUsuario))
            .andExpect(status().isNoContent());

        // usuario #2 NÃO deve conseguir excluir BookEntryGroup que usuario #1 cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry1.getId())
            .header("Authorization", tokenSegundoUsuario))
            .andExpect(status().isNotFound());   
    }    



}