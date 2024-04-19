package com.demo;

import com.demo.controller.admin.AdminUserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest
public class AdminUserControllerTest {
    private MockMvc mockMvc;
    @InjectMocks
    private AdminUserController adminUserController;
    @Mock
    private UserService userService;

    @BeforeEach
    public void setup() {
        this.mockMvc = standaloneSetup(this.adminUserController).build();
    }

    @Test
    public void testUserManage_valid() throws Exception {
        List<User> userList = IntStream.range(0, 11)
                .mapToObj(i -> new User(i, "123", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg"))
                .collect(Collectors.toList());

        Pageable user_pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> mockPage = new PageImpl<>(userList, user_pageable, userList.size());

        when(userService.findByUserID(user_pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attribute("total", 2));// 11条数据，每页10条，共有两页

        verify(userService).findByUserID(user_pageable);
    }
    @Test
    public void testUserManage_emptyPage() throws Exception {
        Pageable user_pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), user_pageable, 0);

        when(userService.findByUserID(user_pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attribute("total", 0));

        verify(userService).findByUserID(user_pageable);
    }

    @Test
    public void testUserAdd_valid() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    public void testUserList_valid() throws Exception {
        List<User> userList = IntStream.range(0, 15)
                .mapToObj(i -> new User(i, "123", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg"))
                .collect(Collectors.toList());

        Pageable user_pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> mockPage = new PageImpl<>(userList, user_pageable, userList.size());
        when(userService.findByUserID(user_pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/userList.do")
                        .param("page", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))   // 获取的数据是json格式
                .andExpect(jsonPath("$", hasSize(15)))  // userList大小为构造的15
                .andExpect(jsonPath("$[0].id", is(0)));

        verify(userService).findByUserID(user_pageable);
    }

    @Test
    public void testUserList_empty() throws Exception {
        Pageable user_pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), user_pageable, 0);
        when(userService.findByUserID(user_pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/userList.do")
                        .param("page", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).findByUserID(user_pageable);
    }

    @Test
    public void testUserList_invalidPage() throws Exception {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/userList.do")
                        .param("page", String.valueOf(-1)))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    public void testUserEdit_valid() throws Exception {
        List<User> userList = IntStream.range(0, 11)
                .mapToObj(i -> new User(i, "123", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg"))
                .collect(Collectors.toList());

        when(userService.findById(0)).thenReturn(userList.get(0));

        mockMvc.perform(get("/user_edit")
                        .param("id", String.valueOf(0)))    // 选择了id=0的user
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeExists("user")) // 验证attribute中被添加了对应user
                .andExpect(model().attribute("user", userList.get(0))); // 验证添加的user是id=0的

        verify(userService).findById(0);
    }

    @Test
    public void testUserEdit_invalidID() throws Exception {
        when(userService.findById(-1)).thenReturn(null);    // 假设返回null
        mockMvc.perform(get("/user_edit")
                        .param("id", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeDoesNotExist("user"));  // 验证attribute中不会被添加user

        verify(userService).findById(-1);
    }

    @Test
    public void testUserEdit_noParam() throws Exception {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/user_edit")); // 不传入参数
        });
    }

    @Test
    public void testUserModify_valid() throws Exception {
        User user = new User(1, "old", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.findByUserID(user.getUserID())).thenReturn(user);
        doNothing().when(userService).updateUser(user);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "new")
                        .param("oldUserID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection()) // 验证重定向
                .andExpect(redirectedUrl("user_manage"));

        assertEquals("new", user.getUserID());  // userID确实被更新为new

        verify(userService).findByUserID("old");
        verify(userService).updateUser(user);
    }

    @Test
    public void testAddUser_valid() throws Exception {
        User user = new User(1, "123", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/addUser.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService).create(new User(0, "123", "abc", "123456", "a@qq.com", "12345678901", 0, ""));
    }

    @Test
    public void testAddUser_alreadyExist() throws Exception {
        User user = new User(1, "123", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/addUser.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        when(userService.create(user)).thenReturn(1);
        mockMvc.perform(post("/addUser.do")
                        .param("userID", user.getUserID())  // 使用相同userID
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService, times(2)).create(any());
    }

    @Test
    public void testAddUser_noPassword() throws Exception {
        User user = new User(1, "123", "abc", "", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/addUser.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService).create(new User(0, "123", "abc", "", "a@qq.com", "12345678901", 0, ""));
    }

    @Test
    public void testCheckUserID_already_new() throws Exception {
        when(userService.countUserID("already")).thenReturn(1); // 已经存在该userID
        when(userService.countUserID("new")).thenReturn(0); // 不存在该userID

        mockMvc.perform(post("/checkUserID.do")
                        .param("userID", "already"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        mockMvc.perform(post("/checkUserID.do")
                        .param("userID", "new"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).countUserID("already");
        verify(userService).countUserID("new");

    }

    @Test
    public void testDelUser_valid() throws Exception {
        doNothing().when(userService).delByID(1);

        mockMvc.perform(post("/delUser.do")
                        .param("id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(userService).delByID(1);

    }

    @Test
    public void testDelUser_invalidID() throws Exception {
        doNothing().when(userService).delByID(1);

        mockMvc.perform(post("/delUser.do")
                        .param("id", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        verify(userService).delByID(-1);

    }

    @Test
    public void testDelUser_noParam() throws Exception {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/delUser.do")) // 不传入参数
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        });
    }
}
