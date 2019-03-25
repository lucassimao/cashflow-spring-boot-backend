package com.lucassimao;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.cashflow.FluxoDeCaixaApplication;
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
@ComponentScan(basePackages={"com.lucassimao.cashflow.config","com.lucassimao"})
public class SecurityTests{

	@Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
	private ListableBeanFactory beanFactory;

    /**
     * Ensuring that unauthenticated requests for RepositoryRestResource implementing
     * TenantAwareRepository are forbidden  
     */
    @Test
    public void unauthenticatedRequestsAreFobidden() {

        Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityTests.class);

        Map<String, Object> beansAnnotatedWithRepositoryRestResource = beanFactory.getBeansWithAnnotation(RepositoryRestResource.class);
        beansAnnotatedWithRepositoryRestResource
            .entrySet().stream().filter(entry -> entry.getValue() instanceof TenantAwareRepository)
            .map(Entry::getKey)
            .map( beanName -> beanFactory.findAnnotationOnBean( beanName , RepositoryRestResource.class))
            .map(RepositoryRestResource::path)
            .forEach(path -> {

                try{

                    logger.debug("Testing endpoint resources at /{}",path);

                    mvc.perform(get("/" + path)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isForbidden());

                    mvc.perform(get("/" + path + "/1")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isForbidden());

                    mvc.perform(post("/" + path)
                        .content(mapper.writeValueAsString(Map.of("description", "created by anonymous user")))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isForbidden());

                    mvc.perform(patch("/" + path + "/1")
                        .content(mapper.writeValueAsString(Map.of("description", "edited by anonymous user")))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isForbidden());

                    mvc.perform(delete("/" + path + "/1"))
                        .andExpect(status().isForbidden());              

                }catch(Exception e){
                    e.printStackTrace();
                    assertFalse(false);
                }

            });


    }
}