package com.demo.controller;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import com.demo.service.MessageVoService;
import com.demo.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.demo.service.MessageService.STATE_PASS;
import static com.demo.service.OrderService.STATE_NO_AUDIT;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    @MockBean
    private NewsService newsService;
    @MockBean
    private VenueService venueService;
    @MockBean
    private MessageVoService messageVoService;
    @MockBean
    private MessageService messageService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void testIndex_valid() throws Exception {
        String venue_name = "venue";
        String description = "this is description";
        String picture = "picture";
        String address = "address";
        String open_time = "08:00";
        String close_time = "21:00";
        Venue venue1 = new Venue(1, venue_name, description, 100, picture, address, open_time, close_time);
        Venue venue2 = new Venue(2, venue_name, description, 300, picture, address, open_time, close_time);
        List<Venue> venue_list = Arrays.asList(venue1, venue2);

        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        News news2 = new News(2, "title2", "content 2", LocalDateTime.now());
        List<News> news_list = Arrays.asList(news1, news2);

        Message message1 = new Message(1,"user1","这是一条未审核的消息",LocalDateTime.now(), STATE_NO_AUDIT);
        Message message2 = new Message(2,"user2","这是一条审核通过的消息",LocalDateTime.now(),STATE_PASS);
        List<Message> message_list = Arrays.asList(message1, message2);

        Page<Message> messages = new PageImpl<>(message_list);

        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(venue_list));
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(news_list));
        when(messageService.findPassState(any(Pageable.class))).thenReturn(messages);
        when(messageVoService.returnVo(anyList())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    // empty
    @Test
    public void testIndex_empty() throws Exception {
        List<Venue> venue_list = new ArrayList<>();
        List<News> news_list = new ArrayList<>();
        List<Message> message_list = new ArrayList<>();

        Page<Message> messages = new PageImpl<>(message_list);

        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(venue_list));
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(news_list));
        when(messageService.findPassState(any(Pageable.class))).thenReturn(messages);
        when(messageVoService.returnVo(anyList())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    //null
    @Test
    public void testIndex_null() throws Exception {
        when(venueService.findAll(any(Pageable.class))).thenReturn(null);
        when(newsService.findAll(any(Pageable.class))).thenReturn(null);
        when(messageService.findPassState(any(Pageable.class))).thenReturn(null);
        when(messageVoService.returnVo(anyList())).thenReturn(null);

        mockMvc.perform(get("/index"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAdminIndex() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));
    }

    @Test
    public void testAdmin_invalidRole() throws Exception {
        User user = new User(1, "userID", "userName", "userPassword", "user@example.com", "12345678910", 0, "userPic");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        mockMvc.perform(get("/admin_index").session(session))
                .andExpect(status().isUnauthorized());

    }

}
