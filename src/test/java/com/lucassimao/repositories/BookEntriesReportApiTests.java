package com.lucassimao.repositories;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.lucassimao.TestUtils;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.MediaTypes;
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
@ComponentScan(basePackageClasses = TestUtils.class)
public class BookEntriesReportApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestUtils testUtils;

    private String authTokenUser1, authTokenUser2;
    private final static ZoneId CLIENT_ZONE = ZoneId.of("America/Adak"); // −10:00

    @Before
    public void setup() throws Exception {

        this.testUtils.criarUsuario("1st user", "usuario-1@bookEntriesReportApiTests.com", "4242424242");
        this.testUtils.criarUsuario("2nd user", "usuario-2@bookEntriesReportApiTests.com", "123321");

        this.authTokenUser1 = this.testUtils.doLogin("usuario-1@bookEntriesReportApiTests.com", "4242424242");
        this.authTokenUser2 = this.testUtils.doLogin("usuario-2@bookEntriesReportApiTests.com", "123321");

        // The first user will have only a house rent expense on 2019/JUNE/30
        BookEntryGroup homeExpensesGroup = new BookEntryGroup();
        homeExpensesGroup.setDescription("Home expenses");
        homeExpensesGroup.setType(BookEntryType.Expense);
        long groupId = this.testUtils.createNewBookEntryGroup(homeExpensesGroup, authTokenUser1);
        homeExpensesGroup.setId(groupId);

        BookEntry bookEntry1 = new BookEntry();
        bookEntry1.setDate(ZonedDateTime.of(LocalDate.of(2019, Month.JUNE, 30), LocalTime.of(20, 50, 5),
                BookEntriesReportApiTests.CLIENT_ZONE));
        bookEntry1.setDescription("house rent");
        bookEntry1.setBookEntryGroup(homeExpensesGroup);
        bookEntry1.setValue(Money.of(1_500, "BRL"));
        this.testUtils.createNewBookEntry(bookEntry1, authTokenUser1);

        // The second user will have only a salary payment on 2019/JULY/29
        BookEntryGroup salaryGroup = new BookEntryGroup();
        salaryGroup.setDescription("Salary");
        salaryGroup.setType(BookEntryType.Income);
        groupId = this.testUtils.createNewBookEntryGroup(salaryGroup, authTokenUser2);
        salaryGroup.setId(groupId);

        BookEntry bookEntry2 = new BookEntry();
        bookEntry2.setDate(ZonedDateTime.of(LocalDate.of(2019, Month.JULY, 29), LocalTime.of(23, 0, 30),
                BookEntriesReportApiTests.CLIENT_ZONE));
        bookEntry2.setDescription("JULY paycheck");
        bookEntry2.setValue(Money.of(10000, "BRL"));
        bookEntry2.setBookEntryGroup(salaryGroup);
        this.testUtils.createNewBookEntry(bookEntry2, authTokenUser2);
    }

    @Test
    public void testSearchingBookEntriesFor1stUser() throws Exception {
        LocalDate julyStart = LocalDate.of(2019, Month.JUNE, 1);
        LocalDate julyEnd = julyStart.withDayOfMonth(julyStart.lengthOfMonth());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String start = formatter.format(julyStart.atStartOfDay().atZone(BookEntriesReportApiTests.CLIENT_ZONE));
        String end = formatter.format(julyEnd.atTime(23, 59, 59).atZone(BookEntriesReportApiTests.CLIENT_ZONE));

        mvc.perform(get("/bookEntries/search/findByInterval")
                    .param("start",start).param("end", end)
                    .header("Authorization",authTokenUser1))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is("house rent")))
            .andExpect(jsonPath("_embedded.bookEntries[0].date", UTCMatcher.utcMatcher(CLIENT_ZONE,"2019-06-30")));
    }

    @Test
    public void testSearchingBookEntriesFor2ndUser() throws Exception {

        LocalDate julyStart = LocalDate.of(2019, Month.JULY, 1);
        LocalDate julyEnd = julyStart.withDayOfMonth(julyStart.lengthOfMonth());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String start = formatter.format(julyStart.atStartOfDay().atZone(BookEntriesReportApiTests.CLIENT_ZONE));
        String end = formatter.format(julyEnd.atTime(23, 59, 59).atZone(BookEntriesReportApiTests.CLIENT_ZONE));


        mvc.perform(get("/bookEntries/search/findByInterval")
                .param("start",start).param("end", end)
                .header("Authorization",authTokenUser2))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is("JULY paycheck")))
            .andExpect(jsonPath("_embedded.bookEntries[0].date", UTCMatcher.utcMatcher(CLIENT_ZONE,"2019-07-29")));

    }    


}