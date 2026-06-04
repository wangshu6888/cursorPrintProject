package com.printims.module.business.controller;

import com.printims.common.annotation.Log;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.module.business.dto.DeliveryPrintVO;
import com.printims.module.business.dto.OrderQuery;
import com.printims.module.business.entity.PrintOrder;
import com.printims.module.business.service.PrintOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
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
import java.util.List;

/**
 * 订单管理接口。
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
public class OrderController {

    private final PrintOrderService printOrderService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','FINANCE','EMPLOYEE')")
    @Operation(summary = "分页查询")
    @GetMapping
    public R<PageResult<PrintOrder>> page(@Valid OrderQuery query) {
        return R.ok(printOrderService.pageQuery(query));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','FINANCE','EMPLOYEE')")
    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<PrintOrder> get(@PathVariable Long id) {
        return R.ok(printOrderService.getById(id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Log("保存订单")
    @Operation(summary = "保存")
    @PostMapping
    public R<Void> save(@Valid @RequestBody PrintOrder body) {
        printOrderService.saveOrder(body);
        return R.ok(null);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Log("更新订单")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PrintOrder body) {
        body.setId(id);
        printOrderService.saveOrder(body);
        return R.ok(null);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Log("删除订单")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        printOrderService.removeById(id);
        return R.ok(null);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','FINANCE','EMPLOYEE')")
    @Operation(summary = "导出Excel")
    @GetMapping("/export")
    public org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody export(OrderQuery query, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        String fileName = java.net.URLEncoder.encode("订单导出", java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        return outputStream -> {
            try {
                printOrderService.exportExcelToStream(outputStream, query);
            } catch (Exception e) {
                // Log exception
            }
        };
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Log("导入订单Excel")
    @Operation(summary = "导入Excel")
    @PostMapping("/import")
    public R<Void> importExcel(MultipartFile file) throws IOException {
        printOrderService.importExcel(file);
        return R.ok(null);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Operation(summary = "送货单打印数据")
    @PostMapping("/print")
    public R<DeliveryPrintVO> print(@RequestBody IdsReq req) {
        return R.ok(printOrderService.buildDeliveryPrint(req.getIds()));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
    @Log("批量出货")
    @Operation(summary = "一键出货")
    @PutMapping("/batch-ship")
    public R<Void> batchShip(@RequestBody IdsReq req) {
        printOrderService.batchShip(req.getIds());
        return R.ok(null);
    }

    @Data
    public static class IdsReq {
        private List<Long> ids;
    }
}
