package com.demo.controller.user;

import com.demo.controller.user.NewsController;
import com.demo.entity.News;
import com.demo.entity.User;
import com.demo.service.NewsService;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @MockBean
    private NewsService newsService;

    @Autowired
    private MockMvc mockMvc;
    

    @Test
    public void testNews_valid() throws Exception {
        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        when(newsService.findById(1)).thenReturn(news1);
        mockMvc.perform(get("/news")
                        .param("newsID", String.valueOf(1)))
                .andExpect(status().isOk());

    }

    //ID为负
    @Test
    public void testNews_invalid() throws Exception {
        assertThrows(NestedServletException.class, ()-> {
                mockMvc.perform(get("/news")
                        .param("newsID", String.valueOf(-1)))
                .andExpect(status().isBadRequest());
        });
    }

    //ID为null
    @Test
    public void testNews_null() throws Exception {
        assertThrows(IllegalArgumentException.class, ()-> {
                mockMvc.perform(get("/news")
                        .param("newsID", null))
                .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testGetNewsList_valid() throws Exception {
        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        News news2 = new News(2, "title2", "content 2", LocalDateTime.now());
        int page = 2;
        List<News> news_list = Arrays.asList(news1, news2);
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("time").descending());

        when(newsService.findAll(any())).thenReturn(new PageImpl<>(news_list, pageable, 2));

        mockMvc.perform(get("/news/getNewsList").param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].newsID", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("title1")))
                .andExpect(jsonPath("$.content[0].content", is("content 1")))
                .andExpect(jsonPath("$.content[1].newsID", is(2)))
                .andExpect(jsonPath("$.content[1].title", is("title2")))
                .andExpect(jsonPath("$.content[1].content", is("content 2")));

        verify(newsService, times(1)).findAll(pageable);
    }

    @Test
    public void testGetNewsList_empty() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("time").descending());
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(Arrays.asList(), pageable, 0));

        mockMvc.perform(get("/news/getNewsList")
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(newsService, times(1)).findAll(pageable);
    }


    //测试page为非正数
    @Test
    public void testGetNewsList_invalidPage() throws Exception {
        int invalidPage = -1;
        assertThrows(NestedServletException.class, ()-> {
                mockMvc.perform(get("/news/getNewsList")
                        .param("page", String.valueOf(invalidPage)))
                .andExpect(status().isBadRequest());
        });
    }

    //测试page为null
    @Test
    public void testGetNewsList_nullPage() throws Exception {
        assertThrows(IllegalArgumentException.class, ()-> {
                mockMvc.perform(get("/news/getNewsList")
                        .param("page", null))
                .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testNewsList_valid() throws Exception {
        News news1 = new News(1, "title1", "content 1", LocalDateTime.now());
        News news2 = new News(2, "title2", "content 2", LocalDateTime.now());
        List<News> news_list = Arrays.asList(news1, news2);
        Pageable news_pageable = PageRequest.of(0, 5, Sort.by("time").descending());

        when(newsService.findAll(news_pageable)).thenReturn(new PageImpl<>(news_list, news_pageable, 2));

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("news_list"))
                .andExpect(model().attribute("news_list", news_list))
                .andExpect(model().attribute("total", newsService.findAll(news_pageable).getTotalPages()));

        verify(newsService, times(3)).findAll(news_pageable);
    }

    @Test
    public void testNewsList_empty() throws Exception {
        List<News> news_list = Arrays.asList();
        Pageable news_pageable = PageRequest.of(0, 10, Sort.by("time").ascending());
        Page<News> emptyPage = new PageImpl<>(news_list, news_pageable, 0);

        when(newsService.findAll(news_pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("news_list", news_list))
                .andExpect(model().attribute("total", 0));

    }

    @Test
    public void testNewsList_null() throws Exception {

        assertThrows(IllegalArgumentException.class, ()-> {
            List<News> news_list = null;
            Pageable news_pageable = PageRequest.of(0, 10, Sort.by("time").ascending());
            Page<News> emptyPage = new PageImpl<>(news_list, news_pageable, 0);

            when(newsService.findAll(news_pageable)).thenReturn(emptyPage);
            mockMvc.perform(get("/news_list"))
                    .andExpect(status().isBadRequest());
        });

    }

}
