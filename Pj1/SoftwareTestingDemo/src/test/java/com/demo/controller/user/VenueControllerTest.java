package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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

    Venue venue1,venue3;
    @BeforeEach
    void setUp() {
        // 新建测试venue
        String venue_name = "venue";
        String description = "this is description";
        String picture = "picture";
        String address = "address";
        String open_time = "08:00";
        String close_time = "21:00";
        venue1 = new Venue(1, venue_name, description, 100, picture, address, open_time, close_time);
        venue3 = new Venue(3, venue_name, description, 300, picture, address, open_time, close_time);
    }

    @Test
    void testToGymPage_normalVID_returned() throws Exception {
        int venueId = 2;

        Venue venue = new Venue(venueId, "Test Venue", "Test Description", 100, "test.jpg", "Test Address", "08:00", "21:00");
        when(venueService.findByVenueID(venueId)).thenReturn(venue);

        mockMvc.perform(MockMvcRequestBuilders.get("/venue").param("venueID", String.valueOf(venueId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("venue"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("venue"))
                .andExpect(MockMvcResultMatchers.model().attribute("venue", venue));
    }

    @Test
    void testVenue_list_normalList_returned() throws Exception {
        List<Venue> venueList = Arrays.asList(venue1, venue3);
        Page<Venue> venuePage = new PageImpl<>(venueList);

        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);

        mockMvc.perform(MockMvcRequestBuilders.get("/venue_list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("venue_list"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("venue_list"))
                .andExpect(MockMvcResultMatchers.model().attribute("venue_list", venueList))
                .andExpect(MockMvcResultMatchers.model().attributeExists("total"))
                .andExpect(MockMvcResultMatchers.model().attribute("total", 1));
    }

    @Test
    void testGetVenueList_normalPage_returned() throws Exception{
        int page = 1;
        List<Venue> venueList = Arrays.asList(venue1, venue3);
        Pageable pageable = PageRequest.of(page-1, 5, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venueList, pageable, 2));

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/venuelist/getVenueList").param("page", String.valueOf(page)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].venueID", is(1)))
                .andExpect(jsonPath("$.content[0].venueName", is("venue")))
                .andExpect(jsonPath("$.content[0].description", is("this is description")))
                .andExpect(jsonPath("$.content[0].price", is(100)))
                .andExpect(jsonPath("$.content[0].picture", is("picture")))
                .andExpect(jsonPath("$.content[0].address", is("address")))
                .andExpect(jsonPath("$.content[0].open_time", is("08:00")))
                .andExpect(jsonPath("$.content[0].close_time", is("21:00")))
                .andExpect(jsonPath("$.content[1].venueID", is(3)))
                .andExpect(jsonPath("$.content[1].venueName", is("venue")))
                .andExpect(jsonPath("$.content[1].description", is("this is description")))
                .andExpect(jsonPath("$.content[1].price", is(300)))
                .andExpect(jsonPath("$.content[1].picture", is("picture")))
                .andExpect(jsonPath("$.content[1].address", is("address")))
                .andExpect(jsonPath("$.content[1].open_time", is("08:00")))
                .andExpect(jsonPath("$.content[1].close_time", is("21:00")))
                .andReturn();
    }

    @Test
    void testGetVenueList_largePageNumber_emptyResponseBody() throws Exception {
        int page = Integer.MAX_VALUE;

        mockMvc.perform(
                MockMvcRequestBuilders.get("/venuelist/getVenueList").param("page", String.valueOf(page))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    void testGetVenueList_negativeOrZeroPageNumber_throwException() throws Exception {
        int page = -1;

        assertThrows(Exception.class, () -> {
            mockMvc.perform(
                    MockMvcRequestBuilders.get("/venuelist/getVenueList").param("page", String.valueOf(page))
                            .contentType(MediaType.APPLICATION_JSON)
            );
        });
    }
}