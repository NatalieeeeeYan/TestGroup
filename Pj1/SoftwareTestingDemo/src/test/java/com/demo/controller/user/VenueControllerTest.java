package com.demo.controller.user;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
class VenueControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

//    @Before
//    public void before() throws Exception {
//        mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
//    }

    @Test
    void toGymPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/venue").param("venueID", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
  }

    @Test
    void venue_list() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/venue_list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(responseBody);
        // 获取 <body> 标签元素
        Element body = doc.body();
        // 检查 <body> 标签中的文本内容不为空
        assertNotNull(body.text(), "HTML body content is not empty");
//        System.out.println("返回的指定内容：" + responseBody);
    }

    @Test
    void testVenue_list() throws Exception{
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/venue").param("venueID", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }
}