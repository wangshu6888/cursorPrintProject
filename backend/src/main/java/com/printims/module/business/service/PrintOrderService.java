package com.printims.module.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.printims.common.api.PageResult;
import com.printims.module.business.dto.DeliveryPrintVO;
import com.printims.module.business.dto.OrderQuery;
import com.printims.module.business.entity.PrintOrder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 订单服务。
 */
public interface PrintOrderService extends IService<PrintOrder> {

    PageResult<PrintOrder> pageQuery(OrderQuery query);

    void saveOrder(PrintOrder entity);

    DeliveryPrintVO buildDeliveryPrint(List<Long> orderIds);

    void exportExcel(HttpServletResponse response, OrderQuery query) throws IOException;

    void exportExcelToStream(java.io.OutputStream outputStream, OrderQuery query) throws IOException;

    void importExcel(MultipartFile file) throws IOException;
}
