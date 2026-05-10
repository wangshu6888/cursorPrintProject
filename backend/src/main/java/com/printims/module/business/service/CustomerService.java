package com.printims.module.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.printims.common.api.PageResult;
import com.printims.module.business.dto.CustomerQuery;
import com.printims.module.business.entity.Customer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 客户服务。
 */
public interface CustomerService extends IService<Customer> {

    PageResult<Customer> pageQuery(CustomerQuery query);

    void saveCustomer(Customer entity);

    void exportExcel(HttpServletResponse response) throws IOException;

    void importExcel(MultipartFile file) throws IOException;
}
