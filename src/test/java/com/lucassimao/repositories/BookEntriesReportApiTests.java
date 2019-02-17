package com.lucassimao.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.lucassimao.TestUtils;
import com.lucassimao.fluxodecaixa.FluxoDeCaixaApplication;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.BeforeClass;
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
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,classes=FluxoDeCaixaApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode=ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackageClasses=TestUtils.class)
public class BookEntriesReportApiTests{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestUtils testUtils;

    private String authTokenUser1, authTokenUser2;
    private static boolean dbOK = false;

    @Before
    public void setup() throws Exception {
        if (dbOK)
            return;

        this.testUtils.criarUsuario("1st user","usuario-1@bookEntriesReportApiTests.com", "4242424242");
        this.testUtils.criarUsuario("2nd user","usuario-2@bookEntriesReportApiTests.com", "123321");

        this.authTokenUser1 = this.testUtils.doLogin("usuario-1@bookEntriesReportApiTests.com", "4242424242");
        this.authTokenUser2 = this.testUtils.doLogin("usuario-2@bookEntriesReportApiTests.com", "123321");


        // The first user will have only a house rent expense on 2019/JUNE/02
        BookEntryGroup homeExpensesGroup = new BookEntryGroup();
        homeExpensesGroup.setDescription("Home expenses");
        homeExpensesGroup.setType(BookEntryType.Expense);
        long groupId = this.testUtils.createNewBookEntryGroup(homeExpensesGroup, authTokenUser1);
        homeExpensesGroup.setId(groupId);
                
        BookEntry bookEntry2 = new BookEntry();
        bookEntry2.setDate(ZonedDateTime.of(LocalDate.of(2019, Month.JUNE, 2), LocalTime.now(), ZoneId.systemDefault()));
        bookEntry2.setDescription("house rent");
        bookEntry2.setBookEntryGroup(homeExpensesGroup);
        bookEntry2.setValue(Money.of(1_500, "BRL"));
        this.testUtils.createNewBookEntry(bookEntry2, authTokenUser1);
        

        // The second user will have only a salary payment on 2019/JULY/27
        BookEntryGroup salaryGroup = new BookEntryGroup();
        salaryGroup.setDescription("Salary");
        salaryGroup.setType(BookEntryType.Income);
        groupId = this.testUtils.createNewBookEntryGroup(salaryGroup, authTokenUser2);
        salaryGroup.setId(groupId);
                
        BookEntry bookEntry1= new BookEntry();
        bookEntry1.setDate(ZonedDateTime.of(LocalDate.of(2019, Month.JULY, 27),LocalTime.now(),ZoneId.systemDefault()));
        bookEntry1.setDescription("JULY paycheck");
        bookEntry1.setValue(Money.of(10000, "BRL"));
        bookEntry1.setBookEntryGroup(salaryGroup);
        this.testUtils.createNewBookEntry(bookEntry1, authTokenUser2);

        dbOK = true;
    }

    @Test
    public void testSearchingBookEntriesFor1stUser() throws Exception {
        LocalDate juneStart = LocalDate.of(2019, Month.JUNE, 1);
        LocalDate juneEnd = juneStart.withDayOfMonth(juneStart.lengthOfMonth());  

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String start = formatter.format(juneStart);
        String end = formatter.format(juneEnd);


        mvc.perform(get("/bookEntries/search/findByInterval")
                    .param("start",start).param("end", end)
                    .header("Authorization",authTokenUser1))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is("house rent")))
            .andExpect(jsonPath("_embedded.bookEntries[0].date", startsWith("2019-06-02")));

    }

    @Test
    public void testSearchingBookEntriesFor2ndUser() throws Exception {
        int monthIndex = Month.JULY.ordinal()+1;
        mvc.perform(get("/bookEntries/search/findByMonth?month=" + monthIndex)
            .header("Authorization",authTokenUser2))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.bookEntries.length()", is(1)))
            .andExpect(jsonPath("_embedded.bookEntries[0].description", is("JULY paycheck")))
            .andExpect(jsonPath("_embedded.bookEntries[0].date", startsWith("2019-07-27")));

    }    


}