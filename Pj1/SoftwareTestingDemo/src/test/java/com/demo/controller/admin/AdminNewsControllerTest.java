package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@WebAppConfiguration
class AdminNewsControllerTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNewsManage() throws Exception {
        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        News news2 = new News(2, "title2", "content 2", LocalDateTime.now());
        List<News> news_list = Arrays.asList(news1, news2);
        Pageable news_pageable= PageRequest.of(0,10, Sort.by("time").ascending());
        Page<News> news = new PageImpl<>(news_list, news_pageable, news_list.size());

        when(newsService.findAll(news_pageable)).thenReturn(news);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", news.getTotalPages()));

        verify(newsService, times(1)).findAll(news_pageable);
    }

    @Test
    void testNewsAdd() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    @Test
    void testNewsEdit() throws Exception {
        News news = new News(1, "title1", "content 1", LocalDateTime.now());
        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(get("/news_edit?newsID=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attribute("news", news));

        verify(newsService, times(1)).findById(1);
    }

    @Test
    void testNewsList() throws Exception {
        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        News news2 = new News(2, "title2", "content 2", LocalDateTime.now());
        List<News> news_list = Arrays.asList(news1, news2);
        Pageable news_pageable= PageRequest.of(0,10, Sort.by("time").descending());
        Page<News> news = new PageImpl<>(news_list, news_pageable, news_list.size());

        when(newsService.findAll(news_pageable)).thenReturn(news);

        mockMvc.perform(get("/newsList.do?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(news1.getTitle())))
                .andExpect(jsonPath("$[0].newsID", is(news1.getNewsID())))
                .andExpect(jsonPath("$[0].content", is(news1.getContent())))
                .andExpect(jsonPath("$[1].title", is(news2.getTitle())))
                .andExpect(jsonPath("$[1].newsID", is(news2.getNewsID())))
                .andExpect(jsonPath("$[1].content", is(news2.getContent())));
    }

    @Test
    void testDelNews() throws Exception {
        mockMvc.perform(post("/delNews.do?newsID=1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService, times(1)).delById(1);


    }

    @Test
    void testModifyNews() throws Exception {
        News news = new News(1, "title1", "content 1", LocalDateTime.now());
        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(post("/modifyNews.do?newsID=1&title=title1&content=content1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

}