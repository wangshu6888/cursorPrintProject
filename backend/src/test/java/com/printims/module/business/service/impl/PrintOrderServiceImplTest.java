package com.printims.module.business.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.api.PageResult;
import com.printims.common.exception.BusinessException;
import com.printims.module.business.dto.DeliveryPrintVO;
import com.printims.module.business.dto.OrderQuery;
import com.printims.module.business.entity.Customer;
import com.printims.module.business.entity.KnifeMold;
import com.printims.module.business.entity.PrintOrder;
import com.printims.module.business.mapper.PrintOrderMapper;
import com.printims.module.business.service.CustomerService;
import com.printims.module.business.service.KnifeMoldService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrintOrderServiceImplTest {

    @Mock
    private PrintOrderMapper printOrderMapper;
    @Mock
    private CustomerService customerService;
    @Mock
    private KnifeMoldService knifeMoldService;

    private PrintOrderServiceImpl printOrderService;

    @BeforeEach
    void setUp() {
        printOrderService = new PrintOrderServiceImpl(customerService, knifeMoldService) {
            @Override
            public PrintOrderMapper getBaseMapper() {
                return printOrderMapper;
            }
        };
    }

    @Test
    void pageQuery() {
        OrderQuery query = new OrderQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("test");

        Page<PrintOrder> mockPage = new Page<>();
        mockPage.setRecords(List.of(new PrintOrder()));
        mockPage.setTotal(1);

        when(printOrderMapper.selectPage(any(), any())).thenReturn(mockPage);

        PageResult<PrintOrder> result = printOrderService.pageQuery(query);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void saveOrder_Success() {
        PrintOrder order = new PrintOrder();
        order.setCustomerId(1L);
        order.setMoldId(2L);
        order.setQuantity(100);
        order.setUnitPrice(new BigDecimal("1.5"));

        Customer customer = new Customer();
        customer.setCustomerName("Test Customer");
        when(customerService.getById(1L)).thenReturn(customer);

        KnifeMold mold = new KnifeMold();
        mold.setMoldName("Test Mold");
        when(knifeMoldService.getById(2L)).thenReturn(mold);

        PrintOrderServiceImpl serviceSpy = spy(printOrderService);
        doReturn(true).when(serviceSpy).saveOrUpdate(any(PrintOrder.class));

        serviceSpy.saveOrder(order);

        assertEquals("Test Customer", order.getCustomerName());
        assertEquals("Test Mold", order.getMoldName());
        assertEquals(new BigDecimal("150.00"), order.getAmount());
        assertNotNull(order.getOrderNo());
        assertEquals(0, order.getShipped());
        verify(serviceSpy).saveOrUpdate(any(PrintOrder.class));
    }

    @Test
    void saveOrder_CustomerNotFound() {
        PrintOrder order = new PrintOrder();
        order.setCustomerId(1L);

        when(customerService.getById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> printOrderService.saveOrder(order));
        assertEquals("客户不存在", ex.getMessage());
    }

    @Test
    void buildDeliveryPrint_Success() {
        PrintOrder order1 = new PrintOrder();
        order1.setId(1L);
        order1.setCustomerId(10L);
        order1.setCustomerName("Customer A");
        order1.setDeliveryNo("DEL-001");

        PrintOrder order2 = new PrintOrder();
        order2.setId(2L);
        order2.setCustomerId(10L);

        when(printOrderMapper.selectBatchIds(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(order1, order2));

        DeliveryPrintVO vo = printOrderService.buildDeliveryPrint(Arrays.asList(1L, 2L));

        assertNotNull(vo);
        assertEquals(10L, vo.getCustomerId());
        assertEquals("Customer A", vo.getCustomerName());
        assertEquals("DEL-001", vo.getDeliveryNo());
        assertEquals(2, vo.getLines().size());
    }

    @Test
    void buildDeliveryPrint_EmptyList() {
        BusinessException ex = assertThrows(BusinessException.class, () -> printOrderService.buildDeliveryPrint(Collections.emptyList()));
        assertEquals("请选择订单", ex.getMessage());
    }

    @Test
    void buildDeliveryPrint_MultipleCustomers() {
        PrintOrder order1 = new PrintOrder();
        order1.setId(1L);
        order1.setCustomerId(10L);

        PrintOrder order2 = new PrintOrder();
        order2.setId(2L);
        order2.setCustomerId(11L);

        when(printOrderMapper.selectBatchIds(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(order1, order2));

        BusinessException ex = assertThrows(BusinessException.class, () -> printOrderService.buildDeliveryPrint(Arrays.asList(1L, 2L)));
        assertEquals("请仅勾选同一客户的订单", ex.getMessage());
    }

    @Test
    void exportExcel() throws IOException {
        OrderQuery query = new OrderQuery();

        Page<PrintOrder> mockPage = new Page<>();
        PrintOrder o1 = new PrintOrder();
        o1.setOrderNo("ORD001");
        mockPage.setRecords(List.of(o1));
        when(printOrderMapper.selectPage(any(), any())).thenReturn(mockPage);

        MockHttpServletResponse response = new MockHttpServletResponse();
        printOrderService.exportExcel(response, query);

        assertTrue(response.getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }
}
