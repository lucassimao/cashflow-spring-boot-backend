package com.lucassimao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.cashflow.CashFlowApplication;
import com.lucassimao.cashflow.model.User;
import com.lucassimao.cashflow.repositories.TenantAwareRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CashFlowApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackages = { "com.lucassimao.cashflow.config", "com.lucassimao" })
public class SecurityTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ListableBeanFactory beanFactory;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Ensuring unauthenticated requests for RepositoryRestResource
     * implementing TenantAwareRepository are forbidden
     */
    @Test
    public void unauthenticatedRequestsAreFobidden() {

        Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityTests.class);

        Map<String, Object> beansAnnotatedWithRepositoryRestResource = beanFactory
                .getBeansWithAnnotation(RepositoryRestResource.class);
        beansAnnotatedWithRepositoryRestResource.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof TenantAwareRepository).map(Entry::getKey)
                .map(beanName -> beanFactory.findAnnotationOnBean(beanName, RepositoryRestResource.class))
                .map(RepositoryRestResource::path).forEach(path -> {

                    try {

                        logger.debug("Testing endpoint resources at /{}", path);

                        mvc.perform(get("/" + path).accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());

                        mvc.perform(get("/" + path + "/1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());

                        mvc.perform(post("/" + path)
                                .content(mapper
                                        .writeValueAsString(Map.of("description", "created by anonymous regularUser")))
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

                        mvc.perform(patch("/" + path + "/1")
                                .content(mapper
                                        .writeValueAsString(Map.of("description", "edited by anonymous regularUser")))
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

                        mvc.perform(delete("/" + path + "/1")).andExpect(status().isForbidden());

                    } catch (Exception e) {
                        e.printStackTrace();
                        assertFalse(false);
                    }

                });
    }

    /**
     * Ensuring regular users can only fire up either a POST request to /users
     * in order to sign up or a PATCH request to edit his own profile. All remaining
     * endpoints are available for admin users
     */
    @Test
    @Transactional
    public void onlySignupEndpointIsOpenForNoRegularUsers() throws Exception {
        long userId = this.testUtils.registerNewUser("User 1", "user1@cashflow.com", "123");
        long user2Id = this.testUtils.registerNewUser("User 2", "user2@cashflow.com", "123");
        assertNotNull(userId);
        assertNotNull(user2Id);

        User regularUser = this.entityManager.find(User.class, userId);
        assertEquals("User 1", regularUser.getName());
        assertEquals("user1@cashflow.com", regularUser.getEmail());
        assertNotEquals("123", regularUser.getEncryptedPassword());
        assertTrue(this.passwordEncoder.matches("123", regularUser.getEncryptedPassword()));
        String user1AuthToken = this.testUtils.doLogin("user1@cashflow.com", "123");


        long adminId = this.testUtils.registerNewUser("admin", "admin@cashflow.com", "123");
        User admin = this.entityManager.find(User.class, adminId);
        // all users registered through the /users endpoint must be registered as regular users
        assertEquals(User.ROLE_USER, admin.getRole());

        // it's only possibile to acquire the administrative role tinkering the database
        admin.setRole(User.ROLE_ADMIN);
        this.entityManager.persist(admin);
        this.entityManager.flush();

        String adminAuthToken =  this.testUtils.doLogin("admin@cashflow.com", "123");

        // only users with ROLE_ADMIN can list all users
        this.mvc.perform(get("/users")).andExpect(status().isForbidden());
        this.mvc.perform(get("/users").header("Authorization", user1AuthToken)).andExpect(status().isForbidden());
        this.mvc.perform(get("/users").header("Authorization", adminAuthToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
            .andExpect(jsonPath("_embedded.users.length()", is(3)));


        this.mvc.perform(get("/users/" + userId)).andExpect(status().isForbidden());
        this.mvc.perform(get("/users/" + user2Id)).andExpect(status().isForbidden());

        // Besides admin users, normal users can only read his own personal information
        this.mvc.perform(get("/users/" + userId).header("Authorization", user1AuthToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("id", is((int)userId)));

        // admin user can read user 1 infromations
        this.mvc.perform(get("/users/" + userId).header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("id", is((int)userId)));  

        // user 1 can't read user2 informations
        this.mvc.perform(get("/users/" + user2Id).header("Authorization", user1AuthToken))
                .andExpect(status().isForbidden());
        // admin user can read user 2 infromations
        this.mvc.perform(get("/users/" + user2Id).header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON_UTF8))
                .andExpect(jsonPath("id", is((int)user2Id)));   
                
        // user 1 can update only his own personal information
        this.mvc.perform(patch("/users"))
                 .andExpect(status().isForbidden());   
        this.mvc.perform(patch("/users/" + user2Id).header("Authorization",user1AuthToken))
                 .andExpect(status().isForbidden());     
        this.mvc.perform(patch("/users/"+ userId)
                        .header("Authorization",user1AuthToken)
                        .content(this.mapper.writeValueAsString(Map.of("name", "User 1 Foo Bar da Silva")))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent());
        
        assertEquals("User 1 Foo Bar da Silva", this.entityManager.find(User.class, userId).getName());

        // admin can update any user information
        this.mvc.perform(patch("/users/"+ userId)
                        .header("Authorization",adminAuthToken)
                        .content(this.mapper.writeValueAsString(Map.of("name", "User 1 -- edited by admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent());         
                        
        this.mvc.perform(patch("/users/"+ user2Id)
                        .header("Authorization",adminAuthToken)
                        .content(this.mapper.writeValueAsString(Map.of("name", "User 2 -- edited by admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent());                          

        assertEquals("User 1 -- edited by admin", this.entityManager.find(User.class, userId).getName());
        assertEquals("User 2 -- edited by admin", this.entityManager.find(User.class, user2Id).getName());
    }
}