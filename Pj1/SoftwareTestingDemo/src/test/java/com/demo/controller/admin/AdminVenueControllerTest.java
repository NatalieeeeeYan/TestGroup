package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.demo.controller.user.VenueController;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
class AdminVenueControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminVenueController adminVenueController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueController venueController;

    private Venue venue;

    @Before
    void setUp() {
        // 新建测试venue
        int venueID = 1;
        String venue_name = "venue";
        String description = "this is description";
        int price = 100;
        String picture = "";
        String address = "address";
        String open_time = "08:00";
        String close_time = "18:00";
        venue = new Venue(venueID, venue_name, description, price, picture, address, open_time, close_time);
    }

    @Test
    void venue_manage_shouldReturnCorrectViewAndModel() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);

        // 添加测试pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        // 模拟 venueService.findAll 方法调用，返回一个带有虚拟数据的 Page 对象
        Page<Venue> page = new PageImpl<>(venues, pageable, 1);
        when(venueService.findAll(any())).thenReturn(page);

        // 执行请求并断言响应
        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 1));

        // 验证 venueService.findAll 方法被调用过
        verify(venueService).findAll(any());
    }

//    @Test
//    void editVenue() {
//    }
//
//    @Test
//    void venue_add() {
//    }
//
//    @Test
//    void getVenueList() {
//    }
//
//    @Test
//    void addVenue() {
//    }
//
//    @Test
//    void modifyVenue() {
//    }
//
//    @Test
//    void delVenue() {
//    }
//
//    @Test
//    void checkVenueName() {
//    }
}