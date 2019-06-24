package com.lucassimao.repositories;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.TestUtils;
import com.lucassimao.cashflow.CashFlowApplication;
import com.lucassimao.cashflow.model.BookEntryGroup;
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
public class BookEntryGroupRepositoryTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EntityManager entityManager;

    private Long firstuserId, secondUserId;

    private String firstUserToken, secondUserToken;

    private BookEntryGroup group2, group1;

    /**
     * Before each test
     *  - registering 2 users
     *  - authenticating both users and storing their authentication tokens
     *  - registering one BookEntryGroup for each user
     * 
     * @throws JsonProcessingException
     * @throws Exception
     */
    @Before
    public void setup() throws JsonProcessingException, Exception {
        Pattern pattern = Pattern.compile("http://localhost(:\\d+)?/bookEntryGroups/(\\d+)$");

        this.firstuserId = testUtils.registerNewUser("Lucas Simao", "lsimaocosta@gmail.com", "123");
        this.firstUserToken = testUtils.doLogin("lsimaocosta@gmail.com", "123");

        this.secondUserId = testUtils.registerNewUser("xpto da silva", "xpto@gmail.com", "123");
        this.secondUserToken = testUtils.doLogin("xpto@gmail.com", "123");

        this.group1 = new BookEntryGroup();
        group1.setDescription("Credit Card bill - 1st user");
        group1.setType(BookEntryType.Expense);

        HttpServletResponse response1 = mvc
                .perform(post("/bookEntryGroups").content(mapper.writeValueAsString(group1))
                        .header("Authorization", firstUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn().getResponse();

        Matcher matcher = pattern.matcher(response1.getHeader("Location"));
        assertTrue(matcher.matches());
        Long creditCardBillId = Long.valueOf(matcher.group(2));
        this.group1.setId(creditCardBillId);

        this.group2 = new BookEntryGroup();
        group2.setDescription("Eletric bill - 2nd user");
        group2.setType(BookEntryType.Expense);

        HttpServletResponse response2 = mvc
                .perform(post("/bookEntryGroups").content(mapper.writeValueAsString(group2))
                        .header("Authorization", secondUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn().getResponse();

        matcher = pattern.matcher(response2.getHeader("Location"));
        assertTrue(matcher.matches());
        Long eletricBillId = Long.valueOf(matcher.group(2));
        this.group2.setId(eletricBillId);
    }

    /**
     * Finding out if each BookEntryGroup registered has the tenantId property equals to the id of the logged in user
     * 
     * @throws Exception
     */
    @Test
    public void checkingTenantId() throws Exception {

        Optional<BookEntryGroup> optional = Optional
                .ofNullable(entityManager.find(BookEntryGroup.class, this.group1.getId()));
        assertTrue(optional.isPresent());
        assertEquals(this.firstuserId, optional.get().getTenantId());

        optional = Optional.ofNullable(entityManager.find(BookEntryGroup.class, this.group2.getId()));
        assertTrue(optional.isPresent());
        assertEquals(this.secondUserId, optional.get().getTenantId());
    }

    /**
     * Testing if a user can read BookEntryGroup registered by another users
     * 
     * @throws Exception
     */
    @Test
    public void userCanReadOnlyHisOwnBookEntryGroups() throws Exception {

        // user #1 tries to request all book entry groups and only receives the ones registered by himself
        mvc.perform(get("/bookEntryGroups").header("Authorization", firstUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("_embedded.bookEntryGroups.length()", is(1)))
                .andExpect(jsonPath("_embedded.bookEntryGroups[0].description", is(group1.getDescription())))
                .andExpect(jsonPath("_embedded.bookEntryGroups[0]._links.self.href",
                        endsWith("bookEntryGroups/" + this.group1.getId())));

        // user #1 can't request BookEntryGroup registered by user #2
        mvc.perform(get("/bookEntryGroups/" + this.group2.getId()).header("Authorization", firstUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

        // user #2 tries to request all book entry groups and only receives the ones registered by himself
        mvc.perform(get("/bookEntryGroups").header("Authorization", secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("_embedded.bookEntryGroups.length()", is(1)))
                .andExpect(jsonPath("_embedded.bookEntryGroups[0].description", is(group2.getDescription())))
                .andExpect(jsonPath("_embedded.bookEntryGroups[0]._links.self.href",
                        endsWith("bookEntryGroups/" + this.group2.getId())));

        // user #2 can't request BookEntryGroup registered by user #2
        mvc.perform(get("/bookEntryGroups/" + this.group1.getId()).header("Authorization", secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

    /**
     * Testing if user can edit BookEntryGroup registered by a different user
     * 
     * @throws Exception
     */
    @Test
    public void userCanEditOnlyHisOwnBookEntryGroups() throws Exception {

        // user #1 can edit a book entry group registered by himself
        mvc.perform(patch("/bookEntryGroups/" + this.group1.getId())
                .content(mapper.writeValueAsString(Map.of("description", "Credit Card bill - edited by 1st user")))
                .header("Authorization", firstUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // user #1 can't edit a book entry group registered by user #2
        mvc.perform(patch("/bookEntryGroups/" + this.group2.getId())
                .content(mapper.writeValueAsString(Map.of("description", "hacked by 1st user")))
                .header("Authorization", firstUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // user #2 can't edit a book entry group registered by user #1
        mvc.perform(patch("/bookEntryGroups/" + this.group1.getId())
                .content(mapper.writeValueAsString(Map.of("description", "hacked by 2nd user")))
                .header("Authorization", secondUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // user #2 can edit a book entry group registered by himself
        mvc.perform(patch("/bookEntryGroups/" + this.group2.getId())
                .content(mapper.writeValueAsString(Map.of("description", "Eletric bill - edited by 2nd user")))
                .header("Authorization", secondUserToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    public void user1CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // user #1 can delete  BookEntryGroup registered by himself
        mvc.perform(delete("/bookEntryGroups/" + this.group1.getId()).header("Authorization", firstUserToken))
                .andExpect(status().isNoContent());

        // user #1 can't delete  BookEntryGroup registered by user #2
        mvc.perform(delete("/bookEntryGroups/" + this.group2.getId()).header("Authorization", firstUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void user2CanDeleteHisOwnBookEntryGroups() throws JsonProcessingException, Exception {
        // user #2 can delete  BookEntryGroup registered by himself
        mvc.perform(delete("/bookEntryGroups/" + this.group2.getId()).header("Authorization", secondUserToken))
                .andExpect(status().isNoContent());

        // user #2 can't delete  BookEntryGroup registered by user #1
        mvc.perform(delete("/bookEntryGroups/" + this.group1.getId()).header("Authorization", secondUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testingPaginatedAccess() throws Exception {

        IntStream.range(0, 199).forEach(index -> {

            BookEntryGroup group = new BookEntryGroup();
            group.setDescription("BookEntryGroup #" + (index + 1));
            group.setType(index % 2 == 0 ? BookEntryType.Expense : BookEntryType.Income);

            try {
                if (index < 99)
                    mvc.perform(post("/bookEntryGroups").content(mapper.writeValueAsString(group))
                            .header("Authorization", firstUserToken).contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated());

                mvc.perform(post("/bookEntryGroups").content(mapper.writeValueAsString(group))
                        .header("Authorization", secondUserToken).contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated());

            } catch (Exception e) {
                e.printStackTrace();
                assertFalse(false);
            }

        });

        // usuario #1 tries to request all book entry groups and only receives the one registered by himself
        mvc.perform(get("/bookEntryGroups").header("Authorization", firstUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("page.size", is(20))).andExpect(jsonPath("page.totalPages", is(5)))
                .andExpect(jsonPath("page.totalElements", is(100)));

        // usuario #2 tries to request all book entry groups and only receives the one registered by himself
        mvc.perform(get("/bookEntryGroups").header("Authorization", secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("page.size", is(20))).andExpect(jsonPath("page.totalPages", is(10)))
                .andExpect(jsonPath("page.totalElements", is(200)));

    }

}