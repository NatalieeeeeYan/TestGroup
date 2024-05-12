package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.*;

import static com.demo.service.MessageService.STATE_PASS;
import static com.demo.service.MessageService.STATE_REJECT;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static com.demo.service.OrderService.STATE_NO_AUDIT;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    Message[] messageArray = new Message[3];
    MessageVo[] messageVoArray = new MessageVo[4];
    List<Message> messages_pass = new ArrayList<>();
    List<Message> messages = new ArrayList<>();
    Message message1, message2, message3;
    User user1;

    @BeforeEach
    void setup() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 4, 11, 13, 14, 14);
        messages = new ArrayList<>();
        message1 = new Message(1, "user1", "这是一条未审核的消息", localDateTime, STATE_NO_AUDIT);
        message2 = new Message(2, "user2", "这是一条审核通过的消息", localDateTime, STATE_PASS);
        message3 = new Message(3, "user3", "这是一条拒绝留言发表", localDateTime, STATE_REJECT);
        user1 = new User(1, "user1", "user_name", "pwd", "email", "phone", 0, "picture");

        for (int i = 0; i < 3; i++) {
            messageArray[i] = new Message(i, "user1", "第" + i + "条", localDateTime, STATE_PASS);
            messages.add(messageArray[i]);
            if (i < 2) {
                messages_pass.add(messageArray[i]);
            }
            messageVoArray[i] = new MessageVo(messageArray[i].getMessageID(), "user1", messageArray[i].getContent(),
                    messageArray[i].getTime(), user1.getUserName(), user1.getPicture(), messageArray[i].getState());
        }
    }

    @Test
    void testMessageList_valid_OK() throws Exception {

        //message
        Pageable message_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> messagesPage = new PageImpl<>(messages, message_pageable, messages.size());
        List<MessageVo> messageVos = new ArrayList<>(Arrays.asList(messageVoArray).subList(0, 3));
        //user
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user1);
        Pageable user_message_pageable = PageRequest.of(0, 5, Sort.by("time").descending());

        //given

        when(messageService.findPassState(any())).thenReturn(messagesPage);
        when(messageVoService.returnVo(any())).thenReturn(messageVos);
        when(messageService.findByUser(any(), any())).thenReturn(new PageImpl<>(messages, user_message_pageable,
                messages.size()));

        //when&then
        mockMvc.perform(get("/message_list").sessionAttrs(sessionAttrs))
                .andExpect(status().isOk())
                .andExpect(view().name("message_list"))
                .andExpect(model().attribute("total", messagesPage.getTotalPages()))
                .andExpect(model().attribute("user_total", messageService.findByUser(user1.getUserID(),
                        user_message_pageable).getTotalPages()));

        verify(messageService).findPassState(any(Pageable.class));
        verify(messageVoService).returnVo(any());
    }

    @Test
    void testMessageList_notLogin_exception() throws Exception {

        //message
        Pageable message_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> messagesPage = new PageImpl<>(messages, message_pageable, messages.size());
        List<MessageVo> messageVos = new ArrayList<>(Arrays.asList(messageVoArray).subList(0, 3));
        //user
        Pageable user_message_pageable = PageRequest.of(0, 5, Sort.by("time").descending());

        when(messageService.findPassState(any())).thenReturn(messagesPage);
        when(messageVoService.returnVo(any())).thenReturn(messageVos);
        when(messageService.findByUser(any(), any())).thenReturn(new PageImpl<>(messages, user_message_pageable,
                messages.size()));

        NestedServletException thrown =
                Assertions.assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/message_list")));
        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));

        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/message_list")
                            .param("page", "1"))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    void testGetMessageList_valid_OK() throws Exception {
        //message pagetable
        int page = 1;
        Pageable message_pageable = PageRequest.of(page - 1, 5, Sort.by("time").descending());
        Page<Message> messages_pass_page = new PageImpl<>(messages_pass, message_pageable, messages_pass.size());
        List<MessageVo> messageVos = new ArrayList<>(Arrays.asList(messageVoArray).subList(0, 2));
        //given
        when(messageService.findPassState(any())).thenReturn(messages_pass_page);
        when(messageVoService.returnVo(any())).thenReturn(messageVos);

        //when&then
        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].messageID").value(0));

        verify(messageService, times(1)).findPassState(message_pageable);
        verify(messageVoService, times(1)).returnVo(messages_pass_page.getContent());

    }

    @Test
    void testGetMessageList_invalidPage_badRequest() throws Exception {
        int invalidPage = -1;

        // Perform the request
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/message/getMessageList")
                            .param("page", String.valueOf(invalidPage)))
                    .andExpect(status().isBadRequest());
        });

        // Verify service methods are not called if input validation fails
        verify(messageService, never()).findPassState(any(Pageable.class));
        verify(messageVoService, never()).returnVo(anyList());
    }

    @Test
    void testUserMessageList_valid_OK() throws Exception {
        Page<Message> page = new PageImpl<>(messages);
        List<MessageVo> messageVos = new ArrayList<>(Arrays.asList(messageVoArray).subList(0, 3));
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user1);

        //given
        when(messageService.findByUser(any(), any())).thenReturn(page);
        when(messageVoService.returnVo(any())).thenReturn(messageVos);

        //when&then
        mockMvc.perform(get("/message/findUserList")
                        .param("page", "1")
                        .sessionAttrs(sessionAttrs))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].messageID").value(0))
                .andExpect(jsonPath("$[0].userID").value("user1"))
                .andExpect(jsonPath("$[1].messageID").value(1))
                .andExpect(jsonPath("$[1].userID").value("user1"));

        verify(messageService).findByUser(eq(user1.getUserID()), any(Pageable.class));
        verify(messageVoService).returnVo(any());
    }

    @Test
    void testUserMessageList_notLogin_exception() {
        NestedServletException thrown =
                Assertions.assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/message/findUserList").param("page", "1")));
        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));
    }

    @Test
    void testSendMessage_valid_OK() throws Exception {

        String userID = "user_for_send";
        String content = "send content";
        when(messageService.create(any())).thenReturn(1);

        //when
        mockMvc.perform(post("/sendMessage")
                        .param("userID", userID)
                        .param("content", content))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        //then
        verify(messageService, times(1)).create(any());
    }

    @Test
    void testSendMessage_nullUserID_exception() throws Exception {

        String content = "send content";
        when(messageService.create(any())).thenReturn(1);

        //when
        assertThrows(IllegalArgumentException.class, () -> {
            mockMvc.perform(post("/sendMessage")
                            .param("userID", null)
                            .param("content", content))
                    .andExpect(status().isBadRequest());
        });
        //then
        verify(messageService, times(1)).create(any());
    }

    @Test
    void testSendMessage_nullContent_exception() throws Exception {

        String userID = "user_for_send";
        when(messageService.create(any())).thenReturn(1);

        //when
        mockMvc.perform(post("/sendMessage")
                        .param("userID", userID)
                        .param("content", null))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        //then
        verify(messageService, times(1)).create(any());
    }

    @Test
    void testModifyMessage_valid_OK() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        Message modify_message = new Message();
        Message test_message = message1;
        modify_message.setMessageID(test_message.getMessageID());
        modify_message.setContent("modify content");
        modify_message.setTime(localDateTime);
        modify_message.setState(1);
        //given
        when(messageService.findById(eq(test_message.getMessageID()))).thenReturn(test_message);

        //when&then
        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", String.valueOf(test_message.getMessageID()))
                        .param("content", modify_message.getContent()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).findById(test_message.getMessageID());
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService).update(captor.capture());
        Message capturedMessage = captor.getValue();
        assertEquals(modify_message.getMessageID(), capturedMessage.getMessageID());
        assertEquals(modify_message.getContent(), capturedMessage.getContent());
        assertEquals(1, capturedMessage.getState());
        assertTrue(capturedMessage.getTime().isAfter(localDateTime));
    }

    @Test
    void testModifyMessage_notExistId_notFound() throws Exception {
        int nonExistentMessageId = 999; // 假设这是一个不存在的ID

        // 给定
        when(messageService.findById(eq(nonExistentMessageId))).thenReturn(null); // 服务层在ID不存在时返回null

        assertThrows(NestedServletException.class, () -> {
            // when
            mockMvc.perform(post("/modifyMessage.do")
                            .param("messageID", String.valueOf(nonExistentMessageId))
                            .param("content", "Some new content"))
                    .andExpect(status().isNotFound());
        });

        // then
        verify(messageService).findById(nonExistentMessageId); // 验证是否调用了messageService.findById方法
        verify(messageService, never()).update(any(Message.class)); // 验证messageService.update方法没有被调用
    }

    @Test
    void testModifyMessage_nullId_badRequest() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            // when
            mockMvc.perform(post("/modifyMessage.do")
                            .param("messageID", null)
                            .param("content", "Some new content"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    void testModifyMessage_invalidUserId_badRequest() throws Exception {
        int invalidUserId = -1;
        assertThrows(NestedServletException.class, () -> {
            // when
            mockMvc.perform(post("/modifyMessage.do")
                            .param("messageID", String.valueOf(invalidUserId))
                            .param("content", "Some new content"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    void testDelMessage_valid_OK() throws Exception {
        int messageId = message1.getMessageID();

        // when
        doNothing().when(messageService).delById(messageId);

        // perform request and assertions
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", String.valueOf(messageId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // verify interactions
        verify(messageService, times(1)).delById(messageId);
    }

    @Test
    void testDelMessage_invalidID_badRequest() throws Exception {
        int messageId = -1;

        // when
        doNothing().when(messageService).delById(messageId);

        assertThrows(AssertionError.class, () -> {
            mockMvc.perform(post("/delMessage.do")
                            .param("messageID", String.valueOf(messageId)))
                    .andExpect(status().isBadRequest());
        });

        // verify interactions
        verify(messageService, times(1)).delById(messageId);
    }

    @Test
    void testDelMessage_notExistId_notFound() throws Exception {
        int nonExistentMessageId = 999; // 假设这是一个不存在的ID

        // 给定
        when(messageService.findById(eq(nonExistentMessageId))).thenReturn(null); // 服务层在ID不存在时返回null

        assertThrows(AssertionError.class, () -> {
            // when
            mockMvc.perform(post("/delMessage.do")
                            .param("messageID", String.valueOf(nonExistentMessageId)))
                    .andExpect(status().isNotFound());
        });

        // then
        verify(messageService).findById(nonExistentMessageId); // 验证是否调用了messageService.findById方法
        verify(messageService, never()).delById(anyInt());// 验证messageService.delById方法没有被调用
    }
}
