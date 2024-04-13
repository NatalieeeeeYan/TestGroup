package com.demo.controller.admin;

import javassist.bytecode.ByteArray;
import org.apache.commons.io.FilenameUtils;
import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.demo.controller.user.VenueController;

import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@WebAppConfiguration
class AdminVenueControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminVenueController adminVenueController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueController venueController;

    private Venue venue1, venue3;
    private Venue newVenue, newVenueWithoutPic, newVenueWithoutDesc, newVenueWithoutPrice, newVenueWithoutOpenTime, newVenueWithoutCloseTime;

    @BeforeEach
    void setUp() {
        String venue_name = "venue";
        String description = "this is description";
        String picture = "picture";
        String address = "address";
        String open_time = "08:00";
        String close_time = "21:00";

        String newVenueName = "new venue";
        String newDescription = "new description";
        String newPicture = "new picture";
        String newAddress = "new address";
        String newOpen_time = "08:00";
        String newClose_time = "21:00";
        int newPrice = 100;
        int newVenueID = 27;

        venue1 = new Venue(1, venue_name, description, 100, picture, address, open_time, close_time);
        venue3 = new Venue(3, venue_name, description, 300, picture, address, open_time, close_time);
        newVenue = new Venue(27, newVenueName, newDescription, newPrice, newPicture, newAddress, newOpen_time, newClose_time);
        newVenueWithoutPic = new Venue(newVenueID, newVenueName, newDescription, newPrice, null, newAddress, newOpen_time, newClose_time);
        newVenueWithoutDesc = new Venue(newVenueID, newVenueName, null, newPrice, newPicture, newAddress, newOpen_time, newClose_time);
        newVenueWithoutPrice = new Venue(newVenueID, newVenueName, newDescription, 0, newPicture, newAddress, newOpen_time, newClose_time);
        newVenueWithoutOpenTime = new Venue(newVenueID, newVenueName, newDescription, newPrice, newPicture, newAddress, null, newClose_time);
        newVenueWithoutCloseTime = new Venue(newVenueID, newVenueName, newDescription, newPrice, newPicture, newAddress, newOpen_time, null);
    }

    @Test
    void testVenueManage_normalList_ok() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 1));

        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 1));

        verify(venueService).findAll(any());
    }

    @Test
    void testEditVenue_normalVenueID_ok() throws Exception {
        int venueId = 1;
        when(venueService.findByVenueID(venueId)).thenReturn(venue1);

        mockMvc.perform(get("/venue_edit").param("venueID",  String.valueOf(venueId)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_edit"))
                .andExpect(model().attribute("venue", venue1));
    }

    @Test
    void testEditVenue_illegalVenueID_badRequest() throws Exception {
        int invaldVenueId = -10;

        mockMvc.perform(get("/venue_edit").param("venueID",  String.valueOf(invaldVenueId)))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("/admin/venue_edit"));
    }

    @Test
    void testEditVenue_nullVenueID_badRequest() throws Exception {
        mockMvc.perform(get("/venue_edit").param("venueID", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditVenue_illegalTypeVenueID_badRequest() throws Exception {
        mockMvc.perform(get("/venue_edit").param("venueID", "illegal"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVenueAdd_legalURL_ok() throws Exception {
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    void testGetVenueList_normalList_ok() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        venues.add(venue3);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 2));

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"venueID\":1,\"venueName\":\"venue\",\"description\":\"this is description\",\"price\":100,\"picture\":\"picture\",\"address\":\"address\",\"open_time\":\"08:00\",\"close_time\":\"21:00\"},{\"venueID\":3,\"venueName\":\"venue\",\"description\":\"this is description\",\"price\":300,\"picture\":\"picture\",\"address\":\"address\",\"open_time\":\"08:00\",\"close_time\":\"21:00\"}]"));

        verify(venueService).findAll(any());
    }

    @Test
    void testGetVenueList_emptyList_emptyReturn () throws Exception {
        List<Venue> venues = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 0));

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(venueService).findAll(any());
    }

    @Test
    void testGetVenueList_normalPage_ok() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        venues.add(venue3);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 2));

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].venueID").value(1))
                .andExpect(jsonPath("$[0].venueName").value("venue"))
                .andExpect(jsonPath("$[0].description").value("this is description"))
                .andExpect(jsonPath("$[0].price").value(100))
                .andExpect(jsonPath("$[0].picture").value("picture"))
                .andExpect(jsonPath("$[0].address").value("address"))
                .andExpect(jsonPath("$[0].open_time").value("08:00"))
                .andExpect(jsonPath("$[0].close_time").value("21:00"))
                .andExpect(jsonPath("$[1].venueID").value(3))
                .andExpect(jsonPath("$[1].venueName").value("venue"))
                .andExpect(jsonPath("$[1].description").value("this is description"))
                .andExpect(jsonPath("$[1].price").value(300))
                .andExpect(jsonPath("$[1].picture").value("picture"))
                .andExpect(jsonPath("$[1].address").value("address"))
                .andExpect(jsonPath("$[1].open_time").value("08:00"))
                .andExpect(jsonPath("$[1].close_time").value("21:00"));
    }

    @Test
    void testGetVenueList_illegalPage_badRequest() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        venues.add(venue3);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 2));

        mockMvc.perform(get("/venueList.do").param("page", "-10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetVenueList_illegalTypePage_badRequest() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        venues.add(venue3);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 2));

        mockMvc.perform(get("/venueList.do").param("page", "illegal"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetVenueList_largePage_badRequest() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 1));

        mockMvc.perform(get("/venueList.do").param("page", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].venueID").value(1))
                .andExpect(jsonPath("$[0].venueName").value("venue"))
                .andExpect(jsonPath("$[0].description").value("this is description"))
                .andExpect(jsonPath("$[0].price").value(100))
                .andExpect(jsonPath("$[0].picture").value("picture"))
                .andExpect(jsonPath("$[0].address").value("address"))
                .andExpect(jsonPath("$[0].open_time").value("08:00"))
                .andExpect(jsonPath("$[0].close_time").value("21:00"));
    }

    @Test
    void testAddVenue_fullVenueInfo_ok () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
        assertNotNull(venueService.findByVenueID(newVenue.getVenueID()));
    }

    @Test
    void testAddVenue_noPicture_ok () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
        assertNotNull(venueService.findByVenueID(newVenue.getVenueID()));
    }

    @Test
    void testAddVenue_zeroOrNegativeVenueID_messageAndBadRequestAndRedirect() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(-1);

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(request().attribute("message", "添加失败！"))
                .andExpect(redirectedUrl("venue_add"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noPrice_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("price", "")
                        .param("description", newVenue.getDescription())
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_illegalPrice_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("price", "-100")
                        .param("description", newVenue.getDescription())
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noOpenTime_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_illegalOpenTime_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("close_time", newVenue.getClose_time())
                        .param("open_time", "25:00"))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noCloseTime_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_illegalCloseTime_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", "25:00"))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noVenueName_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noAddress_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_noDescription_badRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(newVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void testAddVenue_existVenue_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        Venue existVenue = new Venue(72, "场馆2", "desc", 100, "pic", "address", "08:00", "21:00");

        when(venueService.create(any(Venue.class))).thenReturn(existVenue.getVenueID());

        mockMvc.perform(multipart("/addVenue.do")
                .file(picture)
                .param("venueName", existVenue.getVenueName())
                .param("address", existVenue.getAddress())
                .param("price", String.valueOf(existVenue.getPrice()))
                .param("open_time", existVenue.getOpen_time())
                .param("close_time", existVenue.getClose_time()))
                .andExpect(status().isBadRequest());
        verify(venueService).findAll(any());
    }

    @Test
    void testAddVenue_databaseException_messageAndBadRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenThrow(new RuntimeException());

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));
    }

    @Test
    void testModifyVenue_normalModification_ok() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", "modified name")
                        .param("address", "modified address")
                        .param("description", "modified desc")
                        .param("price", "99999")
                        .param("open_time", "13:00")
                        .param("close_time", "19:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).update(any(Venue.class));
        assertEquals(venueService.findByVenueID(venueID).getVenueName(), "modified name");
        assertEquals(venueService.findByVenueID(venueID).getAddress(), "modified address");
        assertEquals(venueService.findByVenueID(venueID).getDescription(), "modified desc");
        assertEquals(venueService.findByVenueID(venueID).getPrice(), 99999);
        assertEquals(venueService.findByVenueID(venueID).getOpen_time(), "13:00");
        assertEquals(venueService.findByVenueID(venueID).getClose_time(), "19:00");
    }

    @Test
    void testModifyVenue_noPicture_ok() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "", "image/jpeg", "test image".getBytes());
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", "modified name")
                        .param("address", "modified address")
                        .param("description", "modified desc")
                        .param("price", "99999")
                        .param("open_time", "13:00")
                        .param("close_time", "19:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).update(any(Venue.class));
        assertEquals(venueService.findByVenueID(venueID).getVenueName(), "modified name");
        assertEquals(venueService.findByVenueID(venueID).getAddress(), "modified address");
        assertEquals(venueService.findByVenueID(venueID).getDescription(), "modified desc");
        assertEquals(venueService.findByVenueID(venueID).getPrice(), 99999);
        assertEquals(venueService.findByVenueID(venueID).getOpen_time(), "13:00");
        assertEquals(venueService.findByVenueID(venueID).getClose_time(), "19:00");
        assertArrayEquals(venueService.findByVenueID(venueID).getPicture().getBytes(), "test image".getBytes());
    }

    @Test
    void testModifyVenue_illegalPrice_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", "-100")
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(0)).update(any(Venue.class));
    }

    @Test
    void testModifyVenue_illegalOpenTime_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", "25:00")
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(0)).update(any(Venue.class));
    }

    @Test
    void testModifyVenue_illegalCloseTime_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", "25:00"))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(0)).update(any(Venue.class));
    }

    @Test
    void testModifyVenue_illegalVenueID_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());
        int venueID = -1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(0)).update(any(Venue.class));
    }

    @Test
    void testModifyVenue_noVenueID_messageAndBadRequest() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", "")
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(0)).update(any(Venue.class));
    }

    @Test
    void testModifyVenue_databaseException_messageAndBadRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenThrow(new RuntimeException());

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", String.valueOf(newVenue.getVenueID()))
                        .param("venueName", newVenue.getVenueName())
                        .param("address", newVenue.getAddress())
                        .param("description", newVenue.getDescription())
                        .param("price", String.valueOf(newVenue.getPrice()))
                        .param("open_time", newVenue.getOpen_time())
                        .param("close_time", newVenue.getClose_time()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));
    }

    @Test
    void testDelVenue_legalVenueID_ok() throws Exception {
        int venueID = 1;

        mockMvc.perform(post("/delVenue.do").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());

        verify(venueService, times(1)).delById(venueID);
        assertNull(venueService.findByVenueID(venueID));
    }

    @Test
    void testDelVenue_illegalVenueID_badRequest() throws Exception {
        int venueID = -100;

        mockMvc.perform(post("/delVenue.do").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isBadRequest());

        verify(venueService, times(0)).delById(venueID);
    }

    @Test
    void testCheckVenueName_legalVenueName_ok() throws Exception {
        String venueName = "venue";

        when(venueService.countVenueName(venueName)).thenReturn(0);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", venueName))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(venueService, times(1)).countVenueName(venueName);
    }

    @Test
    void testCheckVenueName_illegalVenueName_ok() throws Exception {
        String venueName = "";

        when(venueService.countVenueName(venueName)).thenReturn(1);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", venueName))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(venueService, times(1)).countVenueName(venueName);
    }

    @Test
    void testCheckVenueName_databaseException_messageAndBadRequest () throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenThrow(new RuntimeException());

        mockMvc.perform(multipart("/checkVenueName.do")
                        .file(picture)
                        .param("venueName", newVenue.getVenueName()))
                .andExpect(status().isBadRequest())
                .andExpect(redirectedUrl("venue_manage"));
    }
}