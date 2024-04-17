package com.demo.controller.user;

import com.demo.controller.user.UserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testSignUp() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }
    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
    @Test
    public void testLogin_already() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);  // 假设已经登录

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testLoginCheck_userLogin() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");

        when(userService.checkLogin("123","123456")).thenReturn(user);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","123")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"))
                .andExpect(request().sessionAttribute("user", user));   // 验证登录成功，session记录了user状态

        verify(userService).checkLogin("123","123456");
    }

    @Test
    public void testLoginCheck_adminLogin() throws Exception {
        User admin = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 1, "123.jpg");

        when(userService.checkLogin("123","123456")).thenReturn(admin);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","123")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"))
                .andExpect(request().sessionAttribute("admin", admin)); // 验证登录成功，session记录了admin状态

        verify(userService).checkLogin("123","123456");
    }

    @Test
    public void testLoginCheck_fail() throws Exception {
        when(userService.checkLogin("123","123456")).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","123")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andExpect(request().sessionAttribute("user", nullValue())) // 登录失败，session未记录状态
                .andExpect(request().sessionAttribute("admin", nullValue()));

        verify(userService).checkLogin("123","123456");
    }

    @Test
    public void testLoginCheck_nullParam() throws Exception {
        when(userService.checkLogin("123","")).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","123")
                        .param("password",""))  // 密码为空
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andExpect(request().sessionAttribute("user", nullValue()))
                .andExpect(request().sessionAttribute("admin", nullValue()));

        verify(userService).checkLogin("123","");
    }

    @Test
    public void testRegister_valid() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/register.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        verify(userService).create(any());
    }

    @Test
    public void testRegister_repetitiveUserID() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/register.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));
        when(userService.create(user)).thenReturn(1);
        mockMvc.perform(post("/register.do")
                        .param("userID", user.getUserID())  // 重复的userID
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        verify(userService, times(2)).create(any());
    }

    @Test
    public void testRegister_smallPassword() throws Exception {
        User user = new User(1, "123", "123", "1", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.create(user)).thenReturn(1);

        mockMvc.perform(post("/register.do")
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("password", user.getPassword())
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        verify(userService).create(any());
    }

    @Test
    public void testLogout_valid() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);  // 设置原先的登录状态

        mockMvc.perform(get("/logout.do")
                        .session(mockHttpSession))
                .andExpect(request().sessionAttribute("user", nullValue())) // 验证已退出
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testLogout_byAdmin() throws Exception {
        User admin = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 1, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("admin",admin);


        mockMvc.perform(get("/logout.do")
                        .session(mockHttpSession))
                .andExpect(request().sessionAttribute("user", nullValue())) // 验证已退出
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testLogout_without_login() throws Exception {
        mockMvc.perform(get("/logout.do"))
                .andExpect(request().sessionAttribute("user", nullValue())) // 发现可以在未登录情况下直接退出
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testQuit_valid() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);

        mockMvc.perform(get("/quit.do")
                        .session(mockHttpSession))
                .andExpect(request().sessionAttribute("admin", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testQuit_byUser() throws Exception {
        User admin = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 1, "123.jpg");

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("admin",admin);

        mockMvc.perform(get("/quit.do")
                        .session(mockHttpSession))
                .andExpect(request().sessionAttribute("admin", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testUpdateUser_newPassword() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);
        String picture = user.getPicture();

        byte[] content = null;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("picture", "", "image/jpeg,", content); // 不修改图片
        when(userService.findByUserID("123")).thenReturn(user);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockMultipartFile)
                        .session(mockHttpSession)
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("passwordNew", "new")    // 修改密码
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updatedUser = (User) mockHttpSession.getAttribute("user");
        assertEquals(updatedUser.getPassword(),"new");  // 验证密码被修改
        assertEquals(picture, updatedUser.getPicture());    // 验证图片未修改
        verify(userService).findByUserID("123");
    }

    @Test
    public void testUpdateUser_newPicture() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);
        String oldPicture = user.getPicture();

        byte[] content = null;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("picture", "new.jpg", "image/jpeg,", content);  // 修改图片
        when(userService.findByUserID("123")).thenReturn(user);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockMultipartFile)
                        .session(mockHttpSession)
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("passwordNew", "")   // 不修改密码
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updatedUser = (User) mockHttpSession.getAttribute("user");
        assertEquals(updatedUser.getPassword(),"123456");   // 验证密码未修改
        assertNotEquals(oldPicture, updatedUser.getPicture());  // 验证图片被修改
        verify(userService).findByUserID("123");
    }

    @Test
    public void testUpdateUser_sameNewPassword() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);
        String picture = user.getPicture();

        byte[] content = null;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("picture", "", "image/jpeg,", content); // 不修改图片
        when(userService.findByUserID("123")).thenReturn(user);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockMultipartFile)
                        .session(mockHttpSession)
                        .param("userID", user.getUserID())
                        .param("userName", user.getUserName())
                        .param("passwordNew", "123456")    // 与原密码相同
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updatedUser = (User) mockHttpSession.getAttribute("user");
        assertEquals(updatedUser.getPassword(),"123456");
        assertEquals(picture, updatedUser.getPicture());    // 图片未修改
        verify(userService).findByUserID("123");
    }

    @Test
    public void testUpdateUser_anotherUser() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        User user_abc = new User(2, "abc", "abc", "123456", "a@qq.com", "12345678901", 0, "123.jpg");

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);  // 登录了123的用户
        String picture = user.getPicture();

        byte[] content = null;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("picture", "", "image/jpeg,", content); // 不修改图片
        when(userService.findByUserID("123")).thenReturn(user);
        when(userService.findByUserID("abc")).thenReturn(user_abc);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockMultipartFile)
                        .session(mockHttpSession)
                        .param("userID", "abc")
                        .param("userName", user.getUserName())
                        .param("passwordNew", "new")    // 修改密码
                        .param("email", user.getEmail())
                        .param("phone", user.getPhone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updatedUser = (User) mockHttpSession.getAttribute("user");
        assertEquals(updatedUser.getUserID(),"abc");    // 修改了abc用户的信息
        assertEquals(updatedUser.getPassword(),"new");  // 验证密码被修改
        assertEquals(picture, updatedUser.getPicture());    // 验证图片未修改
        verify(userService).findByUserID("abc");
    }

    @Test
    public void testCheckPassword_different() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.findByUserID("123")).thenReturn(user);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID","123")
                        .param("password","new123456")) // 与原密码不同
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        verify(userService).findByUserID("123");
    }
    @Test
    public void testCheckPassword_same() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        when(userService.findByUserID("123")).thenReturn(user);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID","123")
                        .param("password","123456"))    // 与原密码相同
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(userService).findByUserID("123");
    }

    @Test
    public void testUserInfo_login() throws Exception {
        User user = new User(1, "123", "123", "123456", "a@qq.com", "12345678901", 0, "123.jpg");
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user",user);

        mockMvc.perform(get("/user_info")
                        .session(mockHttpSession))  // 在登录状态下访问
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }
    @Test
    public void testUserInfo_logout() throws Exception {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/user_info")); // 在未登录状态下访问
        });
    }

}
