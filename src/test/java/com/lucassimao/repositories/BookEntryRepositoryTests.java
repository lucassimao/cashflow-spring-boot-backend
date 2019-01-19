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

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
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
public class BookEntryRepositoryTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private EntityManager entityManager;

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
        long idPrimeiroUsuario = criarUsuario("Lucas Simao", "papai@noel.com", "1234637463746$%$#$$");
        this.tokenPrimeiroUsuario = doLogin("papai@noel.com", "1234637463746$%$#$$");

        long idSegundoUsuario = criarUsuario("xpto da silva", "xpto@gmail.com", "123");
        this.tokenSegundoUsuario = doLogin("xpto@gmail.com", "123");

        this.bookEntry1 = setupBookEntryGroupAndBookEntry("Fatura de Cartão de Crédito",BookEntryType.Expense,idPrimeiroUsuario, tokenPrimeiroUsuario,"Pagamento do cartão de crédito");
        this.bookEntry2 = setupBookEntryGroupAndBookEntry("Pagamentos a Receber",BookEntryType.Income,idSegundoUsuario, tokenSegundoUsuario,"Boleto do cliente #12345");
    }


    private BookEntry  setupBookEntryGroupAndBookEntry(String bookEntryGroupDescription,BookEntryType bookEntryType,Long idUser,String userToken,String bookEntryDescription) throws Exception, JsonProcessingException {
        final Pattern bookEntryPattern = Pattern.compile("http://localhost(:\\d+)?/bookEntries/(\\d+)$");
        final Pattern bookEntryGroupPattern = Pattern.compile("http://localhost(:\\d+)?/bookEntryGroups/(\\d+)$");
        Random random = new Random(System.currentTimeMillis());
    
        BookEntryGroup group1 = new BookEntryGroup();
        group1.setDescription(bookEntryGroupDescription);
        group1.setType(bookEntryType);

        HttpServletResponse response1 = mvc.perform(post("/bookEntryGroups")
                                            .content(mapper.writeValueAsString(group1))
                                            .header("Authorization", userToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        Matcher matcher = bookEntryGroupPattern.matcher(response1.getHeader("Location"));
        assertTrue(matcher.matches());
        Long bookEntryGroupId = Long.valueOf(matcher.group(2));

        Optional<BookEntryGroup> optional = Optional.ofNullable(entityManager.find(BookEntryGroup.class, bookEntryGroupId));
        assertTrue(optional.isPresent());
        assertEquals(idUser, optional.get().getTenantId());

        BookEntry bookEntry = new BookEntry();
        bookEntry.setDate(LocalDate.now().plusDays(random.nextInt(30)));
        bookEntry.setDescription(bookEntryDescription);
        bookEntry.setValue(Money.of(random.nextInt()/100.0, "BRL"));

        Map bookEntryMap = this.mapper.convertValue(bookEntry, Map.class);
        String url = String.format("http://localhost:%d/bookEntryGroups/%d", this.randomServerPort, bookEntryGroupId);
        bookEntryMap.put("bookEntryGroup", url);

        HttpServletResponse response2 = mvc.perform(post("/bookEntries")
                                            .content(mapper.writeValueAsString(bookEntryMap))
                                            .header("Authorization", userToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        matcher = bookEntryPattern.matcher(response2.getHeader("Location"));
        assertTrue(matcher.matches());
        Long bookEntryId = Long.valueOf(matcher.group(2));
        bookEntry.setId(bookEntryId);

        Optional<BookEntry> optionalBookEntry1 = Optional.ofNullable(entityManager.find(BookEntry.class, bookEntryId));
        assertTrue(optionalBookEntry1.isPresent());
        assertEquals(idUser, optional.get().getTenantId());

        return bookEntry;
    }

    /**
     * Testando se usuario consegue ler BookEntry cadastrados por outros usuários
     * @throws Exception
     */
    @Test
    public void userCanReadOnlyHisOwnBookEntries() throws Exception {

        // usuario #1 tenta requisitar todos e so recebe os que ele cadastrou
        mvc.perform(get("/bookEntries").header("Authorization", tokenPrimeiroUsuario)
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