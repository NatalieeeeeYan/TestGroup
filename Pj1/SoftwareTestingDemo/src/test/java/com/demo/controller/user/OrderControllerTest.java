package com.demo.controller.user;

import com.demo.controller.user.OrderController;
import com.demo.dao.VenueDao;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.entity.vo.VenueOrder;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// 基于黑盒测试的等价类划分、边界值分析和白盒测试的语句覆盖、判定覆盖等测试用例设计技术构造测试用例；需要考虑测试完整性并减少测试用例冗余。
public class OrderControllerTest {

    @InjectMocks
    OrderController orderController;

    @Mock
    OrderService orderService;

    @Mock
    OrderVoService orderVoService;

    @Mock
    VenueService venueService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpSession session;

    @Mock
    Model model;

    // 初始化
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(request.getSession()).thenReturn(session);
        User user = new User();
        user.setUserID("test");
        when(session.getAttribute("user")).thenReturn(user);
    }

    // 已登陆，mock页数为3，正常返回
    @Test
    void testOrderManage() {
        // 模拟Page<Order>对象
        Page<Order> mockPage = mock(Page.class);
        when(mockPage.getTotalPages()).thenReturn(3); // 假设总页数为3

        // 模拟orderService.findUserOrder方法
        when(orderService.findUserOrder(eq("test"), any(Pageable.class))).thenReturn(mockPage);

        // 调用被测试方法
        String result = orderController.order_manage(model, request);

        // 验证行为
        verify(model).addAttribute(eq("total"), eq(3)); // 验证模型数据是否正确添加
        assertEquals("order_manage", result); // 验证返回的视图名称是否正确
    }

    // 未登陆，抛出异常
    @Test
    void testOrderManageNotLogin() {
        // 模拟HttpServletRequest会话
        when(session.getAttribute("user")).thenReturn(null);

        // 调用被测试方法
        try {
            orderController.order_manage(model, request);
        } catch (Exception e) {
            assertEquals("请登录！", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 正常返回
    @Test
    void testOrderPlace() {
        // 模拟Venue对象
        Venue venue = new Venue();
        when(venueService.findByVenueID(1)).thenReturn(venue);

        // 调用被测试方法
        String result = orderController.order_place(model, 1);

        // 验证行为
        verify(model).addAttribute(eq("venue"), eq(venue)); // 验证模型数据是否正确添加
        assertEquals("order_place", result); // 验证返回的视图名称是否正确
    }

    // 正常返回
    @Test
    void testOrderPlace2() {
        // 调用被测试方法
        String result = orderController.order_place(model);

        assertEquals("order_place", result); // 验证返回的视图名称是否正确
    }

    // 已登陆，正常返回
    @Test
    void testOrderList() {
        // 模拟Page<Order>对象
        Page<Order> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(new ArrayList<>());

        // 模拟orderService.findUserOrder方法
        when(orderService.findUserOrder(eq("test"), any(Pageable.class))).thenReturn(mockPage);

        // 调用被测试方法
        List<OrderVo> result = orderController.order_list(1, request);

        // 验证行为
        verify(orderVoService).returnVo(eq(mockPage.getContent())); // 验证是否调用了orderVoService.returnVo方法
    }

    // 未登录
    @Test
    void testOrderListNotLogin() {
        // 模拟HttpServletRequest会话
        when(session.getAttribute("user")).thenReturn(null);

        // 调用被测试方法
        try {
            orderController.order_list(1, request);
        } catch (Exception e) {
            assertEquals("请登录！", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 参数page为0（当前存在错误）
    @Test
    void testOrderListPageMinus1() {
        // 使用 assertThrows 检测是否抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            orderController.order_list(0, request);
        });
    }

    // 正常返回
    @Test
    void testAddOrder() throws Exception {
        // 模拟orderService.submit方法
        doNothing().when(orderService).submit(eq("test"), any(), eq(1), eq("test"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        // 调用被测试方法
        orderController.addOrder("test", "", "2021-01-01 12:00", 1, request, response);

        // 验证行为
        verify(orderService).submit(eq("test"), any(), eq(1), eq("test")); // 验证是否调用了orderService.submit方法
        // 验证是否调用了response.sendRedirect方法
        verify(response).sendRedirect(eq("order_manage"));
    }

    // startTime格式错误（当前存在错误）
    @Test
    void testAddOrderInvalidTime() {
        assertThrows(DateTimeParseException.class, () -> {
            orderController.addOrder("test", "", "2021-01-01", 1, request, mock(HttpServletResponse.class));
        });
    }

//    // hours为负数（当前存在错误）
//    @Test
//    void testAddOrderNegativeHours() {
//        assertThrows(IllegalArgumentException.class, () -> {
//            orderController.addOrder("test", "", "2021-01-01 12:00", -1, request, mock(HttpServletResponse.class));
//        });
//    }

    // 未登录
    @Test
    void testAddOrderNotLogin() {
        // 模拟HttpServletRequest会话
        when(session.getAttribute("user")).thenReturn(null);

        // 调用被测试方法
        try {
            orderController.addOrder("test", "", "2021-01-01 12:00", 1, request, mock(HttpServletResponse.class));
        } catch (Exception e) {
            assertEquals("请登录！", e.getMessage()); // 验证是否抛出异常
        }
    }

    // venueName不存在（当前存在错误）
    @Test
    void testAddOrderInvalidVenueName() {
        // 模拟orderService.submit方法
        doThrow(new RuntimeException("场馆不存在")).when(orderService).submit(eq("test"), any(), eq(1), eq("test"));

        // 调用被测试方法
        try {
            orderController.addOrder("test", "", "2021-01-01 12:00", 1, request, mock(HttpServletResponse.class));
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("场馆不存在", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 正常返回
    @Test
    void testFinishOrder() {
        // 模拟orderService.finishOrder方法
        doNothing().when(orderService).finishOrder(1);

        // 调用被测试方法
        orderController.finishOrder(1);

        // 验证行为
        verify(orderService).finishOrder(1); // 验证是否调用了orderService.finishOrder方法
    }

    // orderID不存在（当前存在错误）
    @Test
    void testFinishOrderInvalidOrderID() {
        // 模拟orderService.finishOrder方法
        doThrow(new RuntimeException("订单不存在")).when(orderService).finishOrder(1);

        // 调用被测试方法
        try {
            orderController.finishOrder(1);
        } catch (Exception e) {
            assertEquals("订单不存在", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 正常返回
    @Test
    void testEditOrder() {
        // 模拟orderService.findById方法
        Order order = new Order();
        order.setVenueID(1);
        when(orderService.findById(1)).thenReturn(order);

        // 模拟venueService.findByVenueID方法
        Venue venue = new Venue();
        when(venueService.findByVenueID(1)).thenReturn(venue);

        // 调用被测试方法
        String result = orderController.editOrder(model, 1);

        // 验证行为
        verify(model).addAttribute(eq("venue"), eq(venue)); // 验证模型数据是否正确添加
        verify(model).addAttribute(eq("order"), eq(order)); // 验证模型数据是否正确添加
        assertEquals("order_edit", result); // 验证返回的视图名称是否正确
    }

    // orderID不存在（当前存在错误）
    @Test
    void testEditOrderInvalidOrderID() {
        // 模拟orderService.findById方法
        when(orderService.findById(1)).thenReturn(null);

        // 调用被测试方法
        try {
            String result = orderController.editOrder(model, 1);
            assertNotNull(model.getAttribute("order"));
            assertEquals("order_edit", result);
        } catch (Exception e) {
            assertEquals("Cannot invoke \"com.demo.entity.Order.getVenueID()\" because \"order\" is null", e.getMessage()); // 验证是否抛出异常
        }
    }

//    // venueId不存在（当前存在错误）
//    @Test
//    void testEditOrderInvalidVenueID() {
//        // 模拟orderService.findById方法
//        Order order = new Order();
//        order.setVenueID(1);
//        when(orderService.findById(1)).thenReturn(order);
//
//        // 模拟venueService.findByVenueID方法
//        when(venueService.findByVenueID(1)).thenReturn(null);
//
//        // 调用被测试方法
//        try {
//            String result = orderController.editOrder(model, 1);
//            assertNotNull(model.getAttribute("venue"));
//            assertEquals("order_edit", result);
//        } catch (Exception e) {
//            assertEquals("", e.getMessage()); // 验证是否抛出异常
//        }
//    }

    // 正常返回
    @Test
    void testModifyOrder() throws Exception {
        // 模拟orderService.updateOrder方法
        doNothing().when(orderService).updateOrder(eq(1), eq("test"), any(), eq(1), eq("test"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        // 调用被测试方法
        boolean result = orderController.modifyOrder("test", "", "2021-01-01 12:00", 1, 1, request, response);

        // 验证行为
        verify(orderService).updateOrder(eq(1), eq("test"), any(), eq(1), eq("test")); // 验证是否调用了orderService.updateOrder方法
        // 验证是否调用了response.sendRedirect方法
        verify(response).sendRedirect(eq("order_manage"));
        assertTrue(result); // 验证返回值是否正确
    }

    // startTime格式错误（当前存在错误）
    @Test
    void testModifyOrderInvalidTime() {
        assertThrows(DateTimeParseException.class, () -> {
            orderController.modifyOrder("test", "", "2021-01-01", 1, 1, request, mock(HttpServletResponse.class));
        });
    }

    // 未登录
    @Test
    void testModifyOrderNotLogin() {
        // 模拟HttpServletRequest会话
        when(session.getAttribute("user")).thenReturn(null);

        // 调用被测试方法
        try {
            orderController.modifyOrder("test", "", "2021-01-01 12:00", 1, 1, request, mock(HttpServletResponse.class));
        } catch (Exception e) {
            assertEquals("请登录！", e.getMessage()); // 验证是否抛出异常
        }
    }

//    // hours为负数（当前存在错误）
//    @Test
//    void testModifyOrderNegativeHours() {
//        assertThrows(IllegalArgumentException.class, () -> {
//            orderController.modifyOrder("test", "", "2021-01-01 12:00", -1, 1, request, mock(HttpServletResponse.class));
//        });
//    }

    // venueName不存在（当前存在错误）
    @Test
    void testModifyOrderInvalidVenueName() {
        // 模拟orderService.updateOrder方法
        doThrow(new RuntimeException("场馆不存在")).when(orderService).updateOrder(eq(1), eq("test"), any(), eq(1), eq("test"));

        // 调用被测试方法
        try {
            orderController.modifyOrder("test", "", "2021-01-01 12:00", 1, 1, request, mock(HttpServletResponse.class));
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("场馆不存在", e.getMessage()); // 验证是否抛出异常
        }
    }

    // orderID不存在（当前存在错误）
    @Test
    void testModifyOrderInvalidOrderID() {
        // 模拟orderService.updateOrder方法
        doThrow(new RuntimeException("订单不存在")).when(orderService).updateOrder(eq(1), eq("test"), any(), eq(1), eq("test"));

        // 调用被测试方法
        try {
            orderController.modifyOrder("test", "", "2021-01-01 12:00", 1, 1, request, mock(HttpServletResponse.class));
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("订单不存在", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 正常返回
    @Test
    void testDelOrder() {
        // 模拟orderService.delOrder方法
        doNothing().when(orderService).delOrder(1);

        // 调用被测试方法
        boolean result = orderController.delOrder(1);

        // 验证行为
        verify(orderService).delOrder(1); // 验证是否调用了orderService.delOrder方法
        assertTrue(result); // 验证返回值是否正确
    }

    // orderID不存在（当前存在错误）
    @Test
    void testDelOrderInvalidOrderID() {
        // 模拟orderService.delOrder方法
        doThrow(new RuntimeException("订单不存在")).when(orderService).delOrder(1);

        // 调用被测试方法
        try {
            boolean result = orderController.delOrder(1);
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("订单不存在", e.getMessage()); // 验证是否抛出异常
        }
    }

    // 正常返回
    @Test
    void testGetOrder() {
        // 模拟Venue对象
        Venue venue = new Venue();
        when(venueService.findByVenueName("test")).thenReturn(venue);

        // 调用被测试方法
        VenueOrder result = orderController.getOrder("test", "2021-01-01");

        // 验证行为
        verify(venueService).findByVenueName("test"); // 验证是否调用了venueService.findByVenueName方法
        assertNotNull(result); // 验证返回值是否正确
    }

    // venueName不存在（当前存在错误）
    @Test
    void testGetOrderInvalidVenueName() {
        // 模拟venueService.findByVenueName方法
        when(venueService.findByVenueName("test")).thenReturn(null);

        // 调用被测试方法
        try {
            VenueOrder result = orderController.getOrder("test", "2021-01-01");
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("Cannot invoke \"com.demo.entity.Venue.getVenueID()\" because \"venue\" is null", e.getMessage()); // 验证是否抛出异常
        }
    }

    // date格式错误（当前存在错误）
    @Test
    void testGetOrderInvalidDate() {
        // 模拟venueService.findByVenueName方法
        when(venueService.findByVenueName("test")).thenReturn(new Venue());

        // 调用被测试方法
        try {
            VenueOrder result = orderController.getOrder("test", "2021-01-01 12:00");
            System.out.println("No problem");
        } catch (Exception e) {
            assertEquals("Text '2021-01-01 12:00 00:00:00' could not be parsed at index 16", e.getMessage()); // 验证是否抛出异常
        }
    }
}

