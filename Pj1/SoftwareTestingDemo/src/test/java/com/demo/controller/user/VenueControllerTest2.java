package com.demo.controller.user;

import com.demo.service.VenueService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@RestController
class VenueControllerTest1 {
    protected Logger logger = LoggerFactory.getLogger(VenueController.class);

    private MockMvc mockMvc;

    @InjectMocks
    private VenueController venueController;

    @Mock
    private VenueService venueService;

    @Before
    public void setUp() {
        // 手动注入 Mock 的 venueService 到 venueController
        venueController = new VenueController();
        mockMvc = MockMvcBuilders.standaloneSetup(venueController).build();
    }

    @Test
    public void testVenueList() throws Exception {
        // 模拟 venueService 的行为
        when(venueService.findByVenueID(2)).thenReturn(null);
        when(venueService.findByVenueName("venueName")).thenReturn(null);
        when(venueService.findAll()).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/venue_list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        logger.info("调用返回的结果：{}", mvcResult.getResponse().getContentAsString());
    }
}
