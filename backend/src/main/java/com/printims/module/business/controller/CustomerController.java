package com.printims.module.business.controller;

import com.printims.common.annotation.Log;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.module.business.dto.CustomerQuery;
import com.printims.module.business.entity.Customer;
import com.printims.module.business.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 客户管理接口。
 */
@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "分页查询")
    @GetMapping
    public R<PageResult<Customer>> page(CustomerQuery query) {
        return R.ok(customerService.pageQuery(query));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<Customer> get(@PathVariable Long id) {
        return R.ok(customerService.getById(id));
    }

    @Log("新增客户")
    @Operation(summary = "新增")
    @PostMapping
    public R<Customer> save(@Valid @RequestBody Customer body) {
        body.setId(null);
        customerService.saveCustomer(body);
        return R.ok(body);
    }

    @Log("更新客户")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody Customer body) {
        body.setId(id);
        customerService.saveCustomer(body);
        return R.ok(null);
    }

    @Log("删除客户")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return R.ok(null);
    }

    @Log("导出客户Excel")
    @Operation(summary = "导出")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        customerService.exportExcel(response);
    }

    @Log("导入客户Excel")
    @Operation(summary = "导入")
    @PostMapping("/import")
    public R<Void> importExcel(MultipartFile file) throws IOException {
        customerService.importExcel(file);
        return R.ok(null);
    }
}
