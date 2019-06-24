package com.lucassimao.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.is;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.TestUtils;
import com.lucassimao.cashflow.CashFlowApplication;
import com.lucassimao.cashflow.model.User;

import org.junit.Test;
import org.junit.runner.RunWith;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserRepositoryTests
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = CashFlowApplication.class)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ComponentScan(basePackages = { "com.lucassimao.cashflow.config", "com.lucassimao" })
public class UserRepositoryTests {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void duplicateUserEmailsAreNotAlowed() throws Exception {
        long user1Id = testUtils.registerNewUser("User1", "user1@cashflow.com", "user1-123");
        testUtils.registerNewUser("User2", "user2@cashflow.com", "user2-123");

        int count = entityManager.createQuery("from User").getResultList().size();
        assertEquals(2,count);

        String user1AuthToken = testUtils.doLogin("user1@cashflow.com", "user1-123");

        this.mvc.perform(MockMvcRequestBuilders.patch("/users/" + user1Id)
                        .header("Authorization",user1AuthToken)
                        .content(this.mapper.writeValueAsString(Map.of("email","user2@cashflow.com"))))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @Transactional
    public void userCanNotUpdateHisRoleAndSignUpdate() throws Exception {
        long user1Id = testUtils.registerNewUser("User1", "user1@cashflow.com", "user1-123");
        String user1AuthToken = testUtils.doLogin("user1@cashflow.com", "user1-123");
        
        User user1 = this.entityManager.find(User.class, user1Id);
        assertEquals(User.ROLE_USER, user1.getRole());
        assertNotNull(user1.getSignUpDate());
        String originalSignUpDateTime = user1.getSignUpDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String newSignUpDateTime = LocalDate.of(2020, Month.APRIL, 15).atTime(15, 15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.mvc.perform(MockMvcRequestBuilders.patch("/users/"+user1Id)
                                                .header("Authorization",user1AuthToken)
                                                .content(this.mapper.writeValueAsString(Map.of("role",User.ROLE_ADMIN,"signUpDate",newSignUpDateTime))))
                                                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // user 1 can send a PATCH request to update role and signUpDate but the original information stays intact
        this.mvc.perform(MockMvcRequestBuilders.get("/users/"+user1Id)
                                                .header("Authorization",user1AuthToken))
                                                .andExpect(MockMvcResultMatchers.status().isOk())
                                                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                                                .andExpect(MockMvcResultMatchers.jsonPath("id", is((int)user1Id)))
                                                .andExpect(MockMvcResultMatchers.jsonPath("role", is(User.ROLE_USER)))
                                                .andExpect(MockMvcResultMatchers.jsonPath("signUpDate", is(originalSignUpDateTime)));


        // entityManager.flush();
        // user1 = this.entityManager.find(User.class, user1Id);
        // assertEquals(User.ROLE_USER, user1.getRole());   
        // assertEquals(originalSignUpDateTime, user1.getSignUpDate());                                                
    }

    
}