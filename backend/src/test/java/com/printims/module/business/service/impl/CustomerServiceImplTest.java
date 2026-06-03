package com.printims.module.business.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.api.PageResult;
import com.printims.module.business.dto.CustomerQuery;
import com.printims.module.business.entity.Customer;
import com.printims.module.business.mapper.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerMapper customerMapper;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl() {
            @Override
            public CustomerMapper getBaseMapper() {
                return customerMapper;
            }
        };
    }

    @Test
    void pageQuery() {
        CustomerQuery query = new CustomerQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("test");

        Page<Customer> mockPage = new Page<>();
        mockPage.setRecords(List.of(new Customer()));
        mockPage.setTotal(1);

        when(customerMapper.selectPage(any(), any())).thenReturn(mockPage);

        PageResult<Customer> result = customerService.pageQuery(query);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void saveCustomer_Success() {
        Customer customer = new Customer();
        customer.setCustomerName("Test Customer");

        CustomerServiceImpl serviceSpy = spy(customerService);
        doReturn(true).when(serviceSpy).saveOrUpdate(any(Customer.class));

        serviceSpy.saveCustomer(customer);

        assertNotNull(customer.getCustomerNo());
        verify(serviceSpy).saveOrUpdate(any(Customer.class));
    }

    @Test
    void exportExcel() throws IOException {
        Customer c1 = new Customer();
        c1.setCustomerName("Customer A");

        CustomerServiceImpl serviceSpy = spy(customerService);
        doReturn(List.of(c1)).when(serviceSpy).list();

        MockHttpServletResponse response = new MockHttpServletResponse();
        serviceSpy.exportExcel(response);

        assertTrue(response.getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }
}
