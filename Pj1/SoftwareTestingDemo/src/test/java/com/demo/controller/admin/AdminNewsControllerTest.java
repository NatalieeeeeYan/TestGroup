package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@WebAppConfiguration
class AdminNewsControllerTest {

    @Mock
    private NewsService newsService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private AdminNewsController adminNewsController;

    @BeforeEach
    public void setUp() {
        this.mockMvc = standaloneSetup(this.adminNewsController).build();
    }

    @Test
    public void testNewsManage_valid() throws Exception {
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
    public void testNewsManage_emptyPage() throws Exception {
        Pageable news_pageable = PageRequest.of(0, 10, Sort.by("time").ascending());
        Page<News> emptyPage = new PageImpl<>(Arrays.asList(), news_pageable, 0);

        when(newsService.findAll(news_pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", 0));

        verify(newsService, times(1)).findAll(news_pageable);
    }


    @Test
    public void testNewsAdd() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }



    @Test
    public void testNewsEdit() throws Exception {
        News news = new News(1, "title1", "content 1", LocalDateTime.now());
        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(get("/news_edit?newsID=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attribute("news", news));

        verify(newsService, times(1)).findById(1);
    }

    // 负值应该为无效
    @Test
    public void testNewsEdit_invalidID() throws Exception{
        when(newsService.findById(-1)).thenReturn(null);

        mockMvc.perform(get("/news_edit?newsID=-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attributeDoesNotExist("news")); // 无效ID时不应该有news属性

        verify(newsService, times(1)).findById(-1);
    }


    @Test
    public void testNewsList() throws Exception {
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

        verify(newsService, times(1)).findAll(news_pageable);
    }

    @Test
    public void testNewsList_empty() throws Exception {
        Pageable news_pageable = PageRequest.of(0, 10, Sort.by("time").descending());
        Page<News> emptyPage = new PageImpl<>(Arrays.asList(), news_pageable, 0);

        when(newsService.findAll(news_pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/newsList.do?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(newsService, times(1)).findAll(news_pageable);
    }


    @Test
    public void testDelNews() throws Exception {
        mockMvc.perform(post("/delNews.do?newsID=1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService, times(1)).delById(1);
    }

    //
    @Test
    public void testDelNews_Null() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            mockMvc.perform(post("/delNews.do")
                            .param("newsID", null))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testDelNews_invalidID() throws Exception {
        int negativeNumber = -10;
        mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(negativeNumber)))
                    .andExpect(status().isBadRequest());

        verify(newsService, times(1)).delById(-1);
    }

    @Test
    public void testDelNews_notExist() throws Exception {
        assertThrows(NestedServletException.class, () -> {
            doThrow(new EmptyResultDataAccessException(1)).when(newsService).delById(999);
            mockMvc.perform(post("/delNews.do").param("newsID", "999"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testModifyNews() throws Exception {
        News news = new News(1, "title1", "content 1", LocalDateTime.now());
        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(post("/modifyNews.do?newsID=1&title=title1&content=content1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

    // 负数ID应该为无效
    @Test
    public void testModifyNews_invalidID() throws Exception {
        int negativeNumber=-10;
        News news = new News(negativeNumber, "title1", "content 1", LocalDateTime.now());
        assertThrows(NestedServletException.class,()->{
            doThrow(new EmptyResultDataAccessException(1)).when(newsService).findById(negativeNumber);
            mockMvc.perform(post("/modifyNews.do")
                            .param("newsID", String.valueOf(news.getNewsID()))
                            .param("title", news.getTitle())
                            .param("content", news.getContent()))
                    .andExpect(status().isBadRequest());
        });
    }

    // ID为null应该为无效

    @Test
    public void testModifyNews_nullID() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            mockMvc.perform(post("/modifyNews.do")
                            .param("newsID", null)
                            .param("title", "title1")
                            .param("content", "content1"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testAddNews() throws Exception {
        mockMvc.perform(post("/addNews.do?title=title1&content=content1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }


    @Test
    public void testAddNews_nullTitle() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            mockMvc.perform(post("/addNews.do")
                            .param("title", null)
                            .param("content", "content1"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testAddNews_nullContent() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            mockMvc.perform(post("/addNews.do")
                            .param("title", "title1")
                            .param("content", null))
                    .andExpect(status().isBadRequest());
        });
    }



}