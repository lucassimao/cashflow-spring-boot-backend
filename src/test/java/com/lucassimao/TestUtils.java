
package com.lucassimao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

@Component
public class TestUtils{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper mapper;

    public String doLogin(String login, String senha) throws JsonProcessingException, Exception {
        Map<String, String> credentials = Map.of("username", login, "password", senha);

        HttpServletResponse response =  mvc.perform(post("/login")
            .content(mapper.writeValueAsString(credentials))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        return response.getHeader("Authorization").replace("Bearer", "").trim();

    }

    public Long criarUsuario(String name, String login, String password) throws Exception {
        Map<String, String> usuario = Map.of("name", name, "email", login, "password", password);

        HttpServletResponse response = mvc.perform(post("/users")
            .content(mapper.writeValueAsString(usuario))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse();

        Pattern pattern = Pattern.compile("http://localhost(:\\d+)?/users/(\\d+)$");
        Matcher matcher = pattern.matcher(response.getHeader("Location"));
        assertTrue(matcher.matches());
        return Long.valueOf(matcher.group(2));
      
    }


    public BookEntry  setupBookEntryGroupAndBookEntry(String bookEntryGroupDescription,BookEntryType bookEntryType,Long idUser,String userToken,String bookEntryDescription) throws Exception, JsonProcessingException {
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

        Map<String,Object> bookEntryMap = this.mapper.convertValue(bookEntry, Map.class);
        bookEntryMap.put("bookEntryGroup", "/bookEntryGroups/" + bookEntryGroupId);

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

}