
package com.lucassimao;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.cashflow.model.BookEntry;
import com.lucassimao.cashflow.model.BookEntryGroup;
import com.lucassimao.cashflow.model.BookEntryType;

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
    private ObjectMapper mapper;

    /**
     * utility to to help authenticate the informed credentials 
     * 
     * @param login user login
     * @param senha password
     * @return authetication token
     * @throws JsonProcessingException
     * @throws Exception
     */
    public String doLogin(String login, String senha) throws JsonProcessingException, Exception {
        Map<String, String> credentials = Map.of("username", login, "password", senha);

        HttpServletResponse response =  mvc.perform(post("/login")
            .content(mapper.writeValueAsString(credentials))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        return response.getHeader("Authorization").replace("Bearer", "").trim();

    }

    /**
     * Cadastra novo usuário no banco de dados
     * 
     * @param name Nome do usuário
     * @param login login para autenticação
     * @param password senha
     * @return pk do usuário no banco de dados
     * @throws Exception
     */
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
        Random random = new Random(System.currentTimeMillis());
    
        BookEntryGroup group1 = new BookEntryGroup();
        group1.setDescription(bookEntryGroupDescription);
        group1.setType(bookEntryType);

        long bookEntryGroupId = createNewBookEntryGroup(group1,userToken);
        group1.setId(bookEntryGroupId);

        BookEntry bookEntry = new BookEntry();
        bookEntry.setDate(ZonedDateTime.now().plusDays(random.nextInt(30)));
        bookEntry.setDescription(bookEntryDescription);
        bookEntry.setValue(Money.of( Math.abs(random.nextInt()/100.0), "BRL"));
        bookEntry.setBookEntryGroup(group1);

        long bookEntryId = createNewBookEntry(bookEntry, userToken);
        bookEntry.setId(bookEntryId);
        return bookEntry;
    }    

    public long createNewBookEntryGroup(BookEntryGroup bookEntryGroup,String userToken)
            throws JsonProcessingException, Exception {
        final Pattern bookEntryGroupPattern = Pattern.compile("http://localhost(:\\d+)?/bookEntryGroups/(\\d+)$");

        HttpServletResponse response1 = mvc.perform(post("/bookEntryGroups")
                                            .content(mapper.writeValueAsString(bookEntryGroup))
                                            .header("Authorization", userToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        Matcher matcher = bookEntryGroupPattern.matcher(response1.getHeader("Location"));
        assertTrue(matcher.matches());
        Long bookEntryGroupId = Long.valueOf(matcher.group(2));        
        return bookEntryGroupId;
    }

    @SuppressWarnings("unchecked")
    public long createNewBookEntry(BookEntry bookEntry,String userToken) throws JsonProcessingException, Exception {
        final Pattern bookEntryPattern = Pattern.compile("http://localhost(:\\d+)?/bookEntries/(\\d+)$");
        Map<String,Object> bookEntryMap = this.mapper.convertValue(bookEntry, Map.class);

        if (bookEntry.getBookEntryGroup() != null)
            bookEntryMap.put("bookEntryGroup", "/bookEntryGroups/" + bookEntry.getBookEntryGroup().getId());

        HttpServletResponse response2 = mvc.perform(post("/bookEntries")
                                            .content(mapper.writeValueAsString(bookEntryMap))
                                            .header("Authorization", userToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isCreated())
                                            .andReturn().getResponse();

        Matcher matcher = bookEntryPattern.matcher(response2.getHeader("Location"));
        assertTrue(matcher.matches());
        Long bookEntryId = Long.valueOf(matcher.group(2));
        return bookEntryId;
    }

}