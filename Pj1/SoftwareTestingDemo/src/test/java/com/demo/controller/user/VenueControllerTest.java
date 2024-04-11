package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.data.domain.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
class VenueControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private VenueService venueService;

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
    void testVenueList() throws Exception {
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
    void testGetVenueList() throws Exception{
        int page = 1;
        Venue venue1 = new Venue(1, "venue1", "description1", 100, "picture1", "address1", "08:00", "21:00");
        Venue venue2 = new Venue(3, "venue3", "description3", 300, "picture3", "address3", "08:30", "22:00");
        List<Venue> venueList = Arrays.asList(venue1, venue2);
        Pageable pageable = PageRequest.of(page-1, 5, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venueList, pageable, 2));

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/venuelist/getVenueList").param("page", String.valueOf(page)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].venueID", is(1)))
                .andExpect(jsonPath("$.content[0].venueName", is("venue1")))
                .andExpect(jsonPath("$.content[0].description", is("description1")))
                .andExpect(jsonPath("$.content[0].price", is(100)))
                .andExpect(jsonPath("$.content[0].picture", is("picture1")))
                .andExpect(jsonPath("$.content[0].address", is("address1")))
                .andExpect(jsonPath("$.content[0].open_time", is("08:00")))
                .andExpect(jsonPath("$.content[0].close_time", is("21:00")))
                .andExpect(jsonPath("$.content[1].venueID", is(3)))
                .andExpect(jsonPath("$.content[1].venueName", is("venue3")))
                .andExpect(jsonPath("$.content[1].description", is("description3")))
                .andExpect(jsonPath("$.content[1].price", is(300)))
                .andExpect(jsonPath("$.content[1].picture", is("picture3")))
                .andExpect(jsonPath("$.content[1].address", is("address3")))
                .andExpect(jsonPath("$.content[1].open_time", is("08:30")))
                .andExpect(jsonPath("$.content[1].close_time", is("22:00")))
                .andReturn();
    }
}