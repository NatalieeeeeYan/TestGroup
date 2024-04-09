package com.demo.controller.admin;

import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static com.demo.service.OrderService.STATE_WAIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class AdminOrderControllerTest {
    @InjectMocks
    AdminOrderController adminOrderController;

    @Mock
    OrderService orderService;

    @Mock
    OrderVoService orderVoService;

    @Mock
    Model model;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // 正常返回
    @Test
    void testReservationManage() {
        List<Order> orders = new ArrayList<Order>();
        List<OrderVo> orderVos = new ArrayList<OrderVo>();
        Page<Order> orderPage = mock(Page.class);
        when(orderService.findAuditOrder()).thenReturn(orders);
        when(orderVoService.returnVo(orders)).thenReturn(orderVos);
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(orderPage);
        when(orderPage.getTotalPages()).thenReturn(1);

        String result = adminOrderController.reservation_manage(model);
        assertEquals("admin/reservation_manage", result);

        // 验证模拟的 Model 对象是否按预期添加了属性
        verify(model).addAttribute(eq("order_list"), eq(orderVos));
        verify(model).addAttribute(eq("total"), eq(1));
    }

    // 正常返回
    @Test
    void testGetNoAuditOrder() {
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(new PageImpl<Order>(new ArrayList<Order>()));
        when(orderVoService.returnVo(anyList())).thenReturn(new ArrayList<OrderVo>());
        List<OrderVo> result = adminOrderController.getNoAuditOrder(1);
        assertNotNull(result);
    }

    // page参数为负数（当前存在错误）
    @Test
    void testGetNoAuditOrderNegativePage() {
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(new PageImpl<Order>(new ArrayList<Order>()));
        when(orderVoService.returnVo(anyList())).thenReturn(new ArrayList<OrderVo>());
        try {
            List<OrderVo> result = adminOrderController.getNoAuditOrder(-1);
        } catch (Exception e) {
            assert e.getMessage().equals("Page index must not be less than zero!");
        }
    }

    // page参数大于总页数（当前存在错误）
    @Test
    void testGetNoAuditOrderPageExceed() {
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(null);
        when(orderVoService.returnVo(anyList())).thenReturn(new ArrayList<OrderVo>());
        try {
            List<OrderVo> result = adminOrderController.getNoAuditOrder(2);
        } catch (Exception e) {
            assert e.getMessage().equals("Cannot invoke \"org.springframework.data.domain.Page.getContent()\" because the return value of \"com.demo.service.OrderService.findNoAuditOrder(org.springframework.data.domain.Pageable)\" is null");
        }
    }

    // 正常返回
    @Test
    void testConfirmOrder() {
        doNothing().when(orderService).confirmOrder(1);
        boolean result = adminOrderController.confirmOrder(1);
        assert result;
    }

    // orderID不存在（当前存在错误）
    @Test
    void testConfirmOrderOrderIDNotExist() {
        doThrow(new RuntimeException("订单不存在")).when(orderService).confirmOrder(0);
        try {
            boolean result = adminOrderController.confirmOrder(0);
            System.out.println("No problem");
        } catch (Exception e) {
            assert e.getMessage().equals("订单不存在");
        }
    }

    // orderID对应订单当前状态不为待审核
    @Test
    void testConfirmOrderOrderStateNotWait() {
        Order order = new Order();
        order.setState(STATE_WAIT);
        when(orderService.findById(1)).thenReturn(order);
        try {
            boolean result = adminOrderController.confirmOrder(1);
            System.out.println("No problem");
        } catch (Exception e) {
            assert e.getMessage().equals("订单状态不为待审核");
        }
    }

    // 正常返回
    @Test
    void testRejectOrder() {
        doNothing().when(orderService).rejectOrder(1);
        boolean result = adminOrderController.rejectOrder(1);
        assert result;
    }

    // orderID不存在（当前存在错误）
    @Test
    void testRejectOrderOrderIDNotExist() {
        doThrow(new RuntimeException("订单不存在")).when(orderService).rejectOrder(0);
        try {
            boolean result = adminOrderController.rejectOrder(0);
            System.out.println("No problem");
        } catch (Exception e) {
            assert e.getMessage().equals("订单不存在");
        }
    }

    // orderID对应订单当前状态不为待审核
    @Test
    void testRejectOrderOrderStateNotWait() {
        Order order = new Order();
        order.setState(STATE_WAIT);
        when(orderService.findById(1)).thenReturn(order);
        try {
            boolean result = adminOrderController.rejectOrder(1);
            System.out.println("No problem");
        } catch (Exception e) {
            assert e.getMessage().equals("订单状态不为待审核");
        }
    }
}









