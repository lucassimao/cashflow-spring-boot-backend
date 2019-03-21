package com.lucassimao.repositories;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.TestUtils;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;
import com.lucassimao.fluxodecaixa.model.Goal;

import org.javamoney.moneta.Money;
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
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackages = { "com.lucassimao.fluxodecaixa.config", "com.lucassimao" })
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FluxoDeCaixaApplication.class)
@AutoConfigureMockMvc
public class GoalRepositoryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestUtils testUtils;

    private String firstUserToken;

    private String secondUserToken;

    private BookEntry bookEntry1,bookEntry2,bookEntry3;

    @Before
    public void setup() throws JsonProcessingException, Exception {
        long idPrimeiroUsuario = testUtils.criarUsuario("Lucas Simao", "papai@noel.com", "1234637463746$%$#$$");
        this.firstUserToken = testUtils.doLogin("papai@noel.com", "1234637463746$%$#$$");

        long idSegundoUsuario = testUtils.criarUsuario("xpto da silva", "xpto@gmail.com", "123");
        this.secondUserToken = testUtils.doLogin("xpto@gmail.com", "123");

        BookEntryGroup group1 = new BookEntryGroup();
        group1.setDescription("Credit card bill");
        group1.setTenantId(idPrimeiroUsuario);
        group1.setType(BookEntryType.Expense);
        group1.setId(testUtils.createNewBookEntryGroup(group1, firstUserToken));


        this.bookEntry1 = new BookEntry();
        this.bookEntry1.setBookEntryGroup(group1);
        this.bookEntry1.setDate(LocalDate.of(2019, Month.FEBRUARY, 5).atStartOfDay(ZoneId.systemDefault()));
        this.bookEntry1.setDescription("VISA's credit card bill from 2019-February");
        this.bookEntry1.setValue(Money.of(BigDecimal.valueOf(50), "BRL"));
        this.bookEntry1.setId(testUtils.createNewBookEntry(this.bookEntry1, firstUserToken));

        this.bookEntry2 = new BookEntry();
        this.bookEntry2.setBookEntryGroup(group1);
        this.bookEntry2.setDate(LocalDate.of(2019, Month.FEBRUARY, 15).atStartOfDay(ZoneId.systemDefault()));
        this.bookEntry2.setDescription("Elletric bill from 2019-February");
        this.bookEntry2.setValue(Money.of(BigDecimal.valueOf(150), "BRL"));
        this.bookEntry2.setId(testUtils.createNewBookEntry(this.bookEntry2, firstUserToken));


        BookEntryGroup group2 = new BookEntryGroup();
        group2.setDescription("Stock dividends");
        group2.setTenantId(idSegundoUsuario);
        group2.setType(BookEntryType.Income);
        group2.setId(testUtils.createNewBookEntryGroup(group2, secondUserToken));


        this.bookEntry3 = new BookEntry();
        this.bookEntry3.setBookEntryGroup(group2);
        this.bookEntry3.setDate(LocalDate.of(2019, Month.FEBRUARY, 15).atStartOfDay(ZoneId.systemDefault()));
        this.bookEntry3.setDescription("June's FB stocks dividends");
        this.bookEntry3.setValue(Money.of(BigDecimal.valueOf(150), "BRL"));
        this.bookEntry3.setId(testUtils.createNewBookEntry(this.bookEntry3, secondUserToken));

    }

    @Test
    public void testGoalForUser1() throws JsonProcessingException, Exception {
        LocalDate februaryStart = LocalDate.of(2019, Month.FEBRUARY, 1);
        LocalDate februaryEnd = februaryStart.withDayOfMonth(februaryStart.lengthOfMonth());
        
        Goal goal = new Goal();
        goal.setBookEntryGroup(this.bookEntry1.getBookEntryGroup());
        goal.setStart(februaryStart.atStartOfDay(ZoneId.systemDefault()));
        goal.setEnd(februaryEnd.atTime(23  , 59, 59).atZone(ZoneId.systemDefault()));
        goal.setMaximum(Money.of(BigDecimal.valueOf(500), "BRL"));
        Map<String,Object> goalMap = this.mapper.convertValue(goal, Map.class);
        goalMap.put("bookEntryGroup", "/bookEntryGroups/" + goal.getBookEntryGroup().getId());

        HttpServletResponse response = mockMvc.perform(post("/goals")
                                            .content(mapper.writeValueAsString(goalMap))
                                            .header("Authorization", this.firstUserToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        mockMvc.perform(get("/goals")
            .header("Authorization", this.firstUserToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.goals.length()", is(1)))
            .andExpect(jsonPath("_embedded.goals[0].bookEntries.length()", is(2)));

    }


    @Test
    public void testGoalForUser2() throws JsonProcessingException, Exception {
        LocalDate februaryStart = LocalDate.of(2019, Month.FEBRUARY, 1);
        LocalDate februaryEnd = februaryStart.withDayOfMonth(februaryStart.lengthOfMonth());
        
        Goal goal = new Goal();
        goal.setBookEntryGroup(this.bookEntry3.getBookEntryGroup());
        goal.setStart(februaryStart.atStartOfDay(ZoneId.systemDefault()));
        goal.setEnd(februaryEnd.atTime(23  , 59, 59).atZone(ZoneId.systemDefault()));
        goal.setMaximum(Money.of(BigDecimal.valueOf(500), "BRL"));
        Map<String,Object> goalMap = this.mapper.convertValue(goal, Map.class);
        goalMap.put("bookEntryGroup", "/bookEntryGroups/" + goal.getBookEntryGroup().getId());

        HttpServletResponse response = mockMvc.perform(post("/goals")
                                            .content(mapper.writeValueAsString(goalMap))
                                            .header("Authorization", this.secondUserToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        mockMvc.perform(get("/goals")
            .header("Authorization", this.secondUserToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.goals.length()", is(1)))
            .andExpect(jsonPath("_embedded.goals[0].bookEntries.length()", is(1)));

    }


}