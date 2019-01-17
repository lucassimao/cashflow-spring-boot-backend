package com.lucassimao.repositories;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class BookEntryGroupRepositoryTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityManager entityManager;

    private Long idPrimeiroUsuario, idSegundoUsuario;

    private String tokenPrimeiroUsuario, tokenSegundoUsuario;

    private final Pattern pattern = Pattern.compile("http://localhost(:\\d+)?/bookEntryGroups/(\\d+)$");

    private BookEntryGroup group2, group1;

    /**
     * Antes de cada teste:
     *  - cadastra 2 usuários, armazenando o login dos mesmos
     *  - realiza o login de cada um e armazena os tokens de autenticação
     *  - faz o cadastro de 2  BookEntryGroup, um p/ cada usuario e guarda referencia pra cada um
     * @throws JsonProcessingException
     * @throws Exception
     */
    @Before
    public void setup() throws JsonProcessingException, Exception {
        this.idPrimeiroUsuario = criarUsuario("Lucas Simao", "lsimaocosta@gmail.com", "123");
        this.tokenPrimeiroUsuario = doLogin("lsimaocosta@gmail.com", "123");

        this.idSegundoUsuario  = criarUsuario("xpto da silva", "xpto@gmail.com", "123");
        this.tokenSegundoUsuario = doLogin("xpto@gmail.com", "123");

        this.group1 = new BookEntryGroup();
        group1.setDescription("Credit Card bill - 1st user");
        group1.setType(BookEntryType.Expense);

        HttpServletResponse response1 = mvc.perform(post("/bookEntryGroups")
            .content(mapper.writeValueAsString(group1))
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse();

        Matcher matcher = pattern.matcher(response1.getHeader("Location"));
        assertTrue(matcher.matches());
        Long creditCardBillId =  Long.valueOf(matcher.group(2));
        this.group1.setId(creditCardBillId);


        this.group2 = new BookEntryGroup();
        group2.setDescription("Eletric bill - 2nd user");
        group2.setType(BookEntryType.Expense);

        HttpServletResponse response2 = mvc.perform(post("/bookEntryGroups")
            .content(mapper.writeValueAsString(group2))
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse();   
            
        
        matcher = pattern.matcher(response2.getHeader("Location"));
        assertTrue(matcher.matches());
        Long eletricBillId =  Long.valueOf(matcher.group(2));
        this.group2.setId(eletricBillId);
    }    

    /**
     * Averiguando se cada instancia cadastrada de 
     * BookEntryGroup tem o tenantId == id do usuario que criou
     * @throws Exception
     */
    @Test
    public void verificandoTenantIdDeCadaBookEntryGroup() throws Exception {

        Optional<BookEntryGroup> optional = Optional.ofNullable(entityManager.find(BookEntryGroup.class, this.group1.getId()));
        assertTrue(optional.isPresent());
        assertEquals(this.idPrimeiroUsuario, optional.get().getTenantId());

        optional = Optional.ofNullable(entityManager.find(BookEntryGroup.class, this.group2.getId()));
        assertTrue(optional.isPresent());
        assertEquals(this.idSegundoUsuario, optional.get().getTenantId());
    }

    /**
     * Testando se usuario consegue ler BookEntryGroup cadastrados por outros usuários
     * @throws Exception
     */
    @Test
    public void userCanReadOnlyHisOwnBookEntryGroups() throws Exception {

        // usuario #1 tenta requisitar todos e so recebe os que ele cadastrou
        mvc.perform(get("/bookEntryGroups").header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntryGroups.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntryGroups[0].description", is(group1.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntryGroups[0]._links.self.href", endsWith("bookEntryGroups/" + this.group1.getId())));

       // usuário #1 tentando requisitar BookEntryGroup cadastrado pelo usuário #2 - n deve conseguir
       mvc.perform(get("/bookEntryGroups/" + this.group2.getId())
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // usuario #2 tenta requisitar todos e so recebe os que ele cadastrou
        mvc.perform(get("/bookEntryGroups")
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntryGroups.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntryGroups[0].description", is(group2.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntryGroups[0]._links.self.href", endsWith("bookEntryGroups/" + this.group2.getId())));

       // usuário #2 tentando requisitar BookEntryGroup cadastrado pelo usuário #1 - n deve conseguir
       mvc.perform(get("/bookEntryGroups/" + this.group1.getId())
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());            
    }


    /**
     * Testando se usuario consegue editar BookEntryGroup cadastrados por outros usuários
     * @throws Exception
     */
    @Test
    public void userCanEditOnlyHisOwnBookEntryGroups() throws Exception {
   
        // usuario #1 deve conseguir atualizar BookEntryGroup que ele cadastrou
        mvc.perform(patch("/bookEntryGroups/"+ this.group1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "Credit Card bill - edited by 1st user")))
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // usuario #1 NÃO deve conseguir atualizar BookEntryGroup que usuario #2 cadastrou
        mvc.perform(patch("/bookEntryGroups/"+ this.group2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 1st user")))
            .header("Authorization", tokenPrimeiroUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());       
            
        // usuario #2 NÃO deve conseguir atualizar BookEntryGroup que usuario #1 cadastrou
        mvc.perform(patch("/bookEntryGroups/"+ this.group1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 2nd user")))
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // usuario #2 deve conseguir atualizar BookEntryGroup que ele cadastrou
        mvc.perform(patch("/bookEntryGroups/"+ this.group2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "Eletric bill - edited by 2nd user")))
            .header("Authorization", tokenSegundoUsuario)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());                

    }

    @Test
    public void user1CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // usuario #1 deve conseguir excluir BookEntryGroup que ele cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.group1.getId())
            .header("Authorization", tokenPrimeiroUsuario))
            .andExpect(status().isNoContent());

        // usuario #1 NÃO deve conseguir excluir BookEntryGroup que usuario #2 cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.group2.getId())
            .header("Authorization", tokenPrimeiroUsuario))
            .andExpect(status().isNotFound());   
    }

    @Test
    public void user2CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // usuario #2 deve conseguir excluir BookEntryGroup que ele cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.group2.getId())
            .header("Authorization", tokenSegundoUsuario))
            .andExpect(status().isNoContent());

        // usuario #2 NÃO deve conseguir excluir BookEntryGroup que usuario #1 cadastrou
        mvc.perform(delete("/bookEntryGroups/"+ this.group1.getId())
            .header("Authorization", tokenSegundoUsuario))
            .andExpect(status().isNotFound());   
    }    



    private String doLogin(String login, String senha) throws JsonProcessingException, Exception {
        Map<String, String> credentials = Map.of("username", login, "password", senha);

        HttpServletResponse response =  mvc.perform(post("/login").content(mapper.writeValueAsString(credentials))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        return response.getHeader("Authorization").replace("Bearer", "").trim();

    }

    private Long criarUsuario(String name, String login, String password) throws Exception {
        Map<String, String> usuario = Map.of("name", name, "email", login, "password", password);

        HttpServletResponse response = mvc.perform(post("/users").content(mapper.writeValueAsString(usuario))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse();

        Pattern pattern = Pattern.compile("http://localhost(:\\d+)?/users/(\\d+)$");
        Matcher matcher = pattern.matcher(response.getHeader("Location"));
        assertTrue(matcher.matches());
        return Long.valueOf(matcher.group(2));
      
    }

}