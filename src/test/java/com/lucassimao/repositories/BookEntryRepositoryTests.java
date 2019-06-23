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
import com.lucassimao.cashflow.CashFlowApplication;
import com.lucassimao.cashflow.model.BookEntry;
import com.lucassimao.cashflow.model.BookEntryType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CashFlowApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackages={"com.lucassimao.cashflow.config","com.lucassimao"})
public class BookEntryRepositoryTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestUtils testUtils;

    private String firstUserToken, secondUserToken;
    private BookEntry bookEntry1, bookEntry2;
   

    /**
     * Before each test:
     *  - register 2 new users
     *  - authenticate each user and storing the authentication tokens returned to be used on further requests
     *  - for each user, register a new BookEntryGroup and BookEntry
     * 
     * @throws JsonProcessingException
     * @throws Exception
     */
    @Before
    public void setup() throws JsonProcessingException, Exception {
        long idPrimeiroUsuario = testUtils.registerNewUser("Lucas Simao", "papai@noel.com", "1234637463746$%$#$$");
        this.firstUserToken = testUtils.doLogin("papai@noel.com", "1234637463746$%$#$$");

        long idSegundoUsuario = testUtils.registerNewUser("xpto da silva", "xpto@gmail.com", "123");
        this.secondUserToken = testUtils.doLogin("xpto@gmail.com", "123");

        this.bookEntry1 = testUtils.setupBookEntryGroupAndBookEntry("Fatura de Cartão de Crédito",BookEntryType.Expense,idPrimeiroUsuario, firstUserToken,"Pagamento do cartão de crédito");
        this.bookEntry2 = testUtils.setupBookEntryGroupAndBookEntry("Pagamentos a Receber",BookEntryType.Income,idSegundoUsuario, secondUserToken,"Boleto do cliente #12345");
    }



    /**
     * Testing if an user can read BookEntry entities created by a different user
     * @throws Exception
     */
    @Test
    public void userCanReadOnlyHisOwnBookEntries() throws Exception {

        // user #1 tries to request all book entries and only receives the entities registered by himself
        mvc.perform(get("/bookEntries")
            .header("Authorization", firstUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is(bookEntry1.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntries[0]._links.self.href", endsWith("bookEntries/" + this.bookEntry1.getId())));

       // user #1 sends a request for a BookEntry registered by the user #2 and receives a NotFound response
       mvc.perform(get("/bookEntries/" + this.bookEntry2.getId())
            .header("Authorization", firstUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // user #2 tries to request all book entries and only receives the entities registered by himself
        mvc.perform(get("/bookEntries")
            .header("Authorization", secondUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is(bookEntry2.getDescription())))
            .andExpect(jsonPath("_embedded.bookEntries[0]._links.self.href", endsWith("bookEntries/" + this.bookEntry2.getId())));

       // user #2 sends a request for a BookEntry registered by the user #2 and receives a NotFound response
       mvc.perform(get("/bookEntries/" + this.bookEntry1.getId())
            .header("Authorization", secondUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());            
    }


    /**
     * Testing if a user can edit BookEntries not created by himself
     * @throws Exception
     */
    @Test
    public void userCanEditOnlyHisOwnBookEntryGroups() throws Exception {
   
        // user #1 can update the BookEntry registered by himself
        mvc.perform(patch("/bookEntries/"+ this.bookEntry1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "edited by 1st user")))
            .header("Authorization", firstUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // user #1 can't update the BookEntry registered by the user #2
        mvc.perform(patch("/bookEntries/"+ this.bookEntry2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 1st user")))
            .header("Authorization", firstUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());       
            
        // user #2 can't update the BookEntry registered by the user #1
        mvc.perform(patch("/bookEntries/"+ this.bookEntry1.getId())
            .content(mapper.writeValueAsString(Map.of("description", "hacked by 2nd user")))
            .header("Authorization", secondUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        // user #2 can update the BookEntry registered by himself
        mvc.perform(patch("/bookEntries/"+ this.bookEntry2.getId())
            .content(mapper.writeValueAsString(Map.of("description", "edited by 2nd user")))
            .header("Authorization", secondUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());                

    }

    @Test
    public void user1CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // user #1 can delete BookEntry and BookEntryGroups registered by himself
        mvc.perform(delete("/bookEntries/"+ this.bookEntry1.getId())
            .header("Authorization", firstUserToken))
            .andExpect(status().isNoContent());

        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry1.getBookEntryGroup().getId() )
            .header("Authorization", firstUserToken))
            .andExpect(status().isNoContent());

        // user #1 can't delete BookEntry and BookEntryGroups registered by user #2
        mvc.perform(delete("/bookEntries/"+ this.bookEntry2.getId())
            .header("Authorization", firstUserToken))
            .andExpect(status().isNotFound());

        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry2.getId())
            .header("Authorization", firstUserToken))
            .andExpect(status().isNotFound());   
    }

    @Test
    public void user2CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // user #2 can delete BookEntry and BookEntryGroups registered by himself
        mvc.perform(delete("/bookEntries/"+ this.bookEntry2.getId())
            .header("Authorization", secondUserToken))
            .andExpect(status().isNoContent());

        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry2.getBookEntryGroup().getId())
            .header("Authorization", secondUserToken))
            .andExpect(status().isNoContent());

        // user #2 can't delete BookEntry  and BookEntryGroups registered by user #1
        mvc.perform(delete("/bookEntries/"+ this.bookEntry1.getId())
            .header("Authorization", secondUserToken))
            .andExpect(status().isNotFound());

        mvc.perform(delete("/bookEntryGroups/"+ this.bookEntry1.getBookEntryGroup().getId())
            .header("Authorization", secondUserToken))
            .andExpect(status().isNotFound());   
    }    



}