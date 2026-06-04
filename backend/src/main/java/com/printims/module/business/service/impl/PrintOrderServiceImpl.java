package com.printims.module.business.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.common.api.PageResult;
import com.printims.common.exception.BusinessException;
import com.printims.module.business.dto.DeliveryPrintVO;
import com.printims.module.business.dto.OrderQuery;
import com.printims.module.business.entity.Customer;
import com.printims.module.business.entity.KnifeMold;
import com.printims.module.business.entity.PrintOrder;
import com.printims.module.business.excel.OrderExcelRow;
import com.printims.module.business.excel.OrderExportRow;
import com.printims.module.business.mapper.PrintOrderMapper;
import com.printims.module.business.service.CustomerService;
import com.printims.module.business.service.KnifeMoldService;
import com.printims.module.business.service.PrintOrderService;
import com.printims.util.BizNoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单服务实现。
 */
@Service
@RequiredArgsConstructor
public class PrintOrderServiceImpl extends ServiceImpl<PrintOrderMapper, PrintOrder> implements PrintOrderService {

    private final CustomerService customerService;
    private final KnifeMoldService knifeMoldService;

    @Override
    public PageResult<PrintOrder> pageQuery(OrderQuery query) {
        Page<PrintOrder> p = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PrintOrder> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String k = query.getKeyword().trim();
            w.and(x -> x.like(PrintOrder::getOrderNo, k)
                    .or().like(PrintOrder::getCustomerName, k)
                    .or().like(PrintOrder::getPrintName, k)
                    .or().like(PrintOrder::getDeliveryNo, k));
        }
        if (query.getStartDate() != null) {
            w.ge(PrintOrder::getOrderDate, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            w.le(PrintOrder::getOrderDate, query.getEndDate().plusDays(1).atStartOfDay());
        }
        if (query.getShipped() != null) {
            w.eq(PrintOrder::getShipped, query.getShipped());
        }
        w.orderByDesc(PrintOrder::getOrderDate);
        Page<PrintOrder> result = page(p, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(PrintOrder entity) {
        // Quick add for Customer (with dedup check)
        if (entity.getCustomerId() == null && StringUtils.hasText(entity.getCustomerName())) {
            Customer existing = customerService.lambdaQuery()
                    .eq(Customer::getCustomerName, entity.getCustomerName().trim())
                    .one();
            if (existing != null) {
                entity.setCustomerId(existing.getId());
            } else {
                Customer newCustomer = new Customer();
                newCustomer.setCustomerName(entity.getCustomerName().trim());
                newCustomer.setPhone(null);
                customerService.saveCustomer(newCustomer);
                entity.setCustomerId(newCustomer.getId());
            }
        }

        Customer c = customerService.getById(entity.getCustomerId());
        if (c == null) {
            throw new BusinessException("客户不存在");
        }
        entity.setCustomerName(c.getCustomerName());

        // Quick add for KnifeMold
        if (entity.getMoldId() == null && StringUtils.hasText(entity.getMoldName())) {
            KnifeMold newMold = new KnifeMold();
            newMold.setMoldName(entity.getMoldName());
            newMold.setShapeType("CUSTOM");
            newMold.setAreaCode(null);
            newMold.setShelfNo(null);
            newMold.setLayerNo(null);
            newMold.setPositionNo(null);
            knifeMoldService.saveMold(newMold);
            entity.setMoldId(newMold.getId());
        }

        if (entity.getMoldId() != null) {
            KnifeMold m = knifeMoldService.getById(entity.getMoldId());
            if (m != null) {
                entity.setMoldName(m.getMoldName());
            }
        }
        if (entity.getQuantity() != null && entity.getUnitPrice() != null) {
            BigDecimal amt = entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            // Forcefully override frontend amount
            entity.setAmount(amt);
        } else {
            entity.setAmount(BigDecimal.ZERO);
        }
        if (!StringUtils.hasText(entity.getOrderNo())) {
            entity.setOrderNo(BizNoUtil.orderNo());
        }
        if (entity.getShipped() == null) {
            entity.setShipped(0);
        }
        saveOrUpdate(entity);
    }

    @Override
    public DeliveryPrintVO buildDeliveryPrint(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException("请选择订单");
        }
        List<PrintOrder> orders = listByIds(orderIds);
        if (orders.size() != orderIds.size()) {
            throw new BusinessException("部分订单不存在");
        }
        Set<Long> customerIds = new HashSet<>();
        for (PrintOrder o : orders) {
            customerIds.add(o.getCustomerId());
        }
        if (customerIds.size() > 1) {
            throw new BusinessException("请仅勾选同一客户的订单");
        }
        PrintOrder first = orders.get(0);
        DeliveryPrintVO vo = new DeliveryPrintVO();
        vo.setCustomerId(first.getCustomerId());
        vo.setCustomerName(first.getCustomerName());
        vo.setDeliveryNo(first.getDeliveryNo());
        vo.setPrintTime(LocalDateTime.now());
        List<DeliveryPrintVO.Line> lines = new ArrayList<>();
        for (PrintOrder o : orders) {
            DeliveryPrintVO.Line line = new DeliveryPrintVO.Line();
            line.setOrderNo(o.getOrderNo());
            line.setPrintName(o.getPrintName());
            line.setQuantity(o.getQuantity());
            line.setUnitPrice(o.getUnitPrice());
            line.setAmount(o.getAmount());
            line.setRemark(o.getRemark());
            lines.add(line);
        }
        vo.setLines(lines);
        return vo;
    }

    @Override
    public void exportExcel(HttpServletResponse response, OrderQuery query) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("订单导出", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        exportExcelToStream(response.getOutputStream(), query);
    }

    @Override
    public void exportExcelToStream(java.io.OutputStream outputStream, OrderQuery query) throws IOException {
        query.setPageNum(1);
        query.setPageSize(5000);
        List<PrintOrder> list = pageQuery(query).getRecords();
        List<OrderExportRow> rows = list.stream().map(o -> {
            OrderExportRow r = new OrderExportRow();
            r.setOrderNo(o.getOrderNo());
            r.setOrderDate(o.getOrderDate());
            r.setDeliveryNo(o.getDeliveryNo());
            r.setPrintName(o.getPrintName());
            r.setQuantity(o.getQuantity());
            r.setUnitPrice(o.getUnitPrice());
            r.setAmount(o.getAmount());
            r.setScheduleNo(o.getScheduleNo());
            r.setCustomerName(o.getCustomerName());
            r.setMoldName(o.getMoldName());
            r.setMaterial(o.getMaterial());
            r.setShipped(o.getShipped() != null && o.getShipped() == 1 ? "是" : "否");
            r.setDeliveryDate(o.getDeliveryDate());
            r.setRemark(o.getRemark());
            return r;
        }).toList();
        EasyExcel.write(outputStream, OrderExportRow.class).sheet("订单").doWrite(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) throws IOException {
        List<OrderExcelRow> rows = EasyExcel.read(file.getInputStream()).head(OrderExcelRow.class).sheet().doReadSync();
        for (OrderExcelRow r : rows) {
            if (r.getOrderDate() == null || !StringUtils.hasText(r.getCustomerName())
                    || !StringUtils.hasText(r.getPrintName()) || r.getQuantity() == null || r.getUnitPrice() == null
                    || !StringUtils.hasText(r.getDeliveryNo())) {
                continue;
            }
            Customer c = customerService.lambdaQuery()
                    .eq(Customer::getCustomerName, r.getCustomerName().trim())
                    .one();
            if (c == null) {
                throw new BusinessException("导入失败：客户不存在 " + r.getCustomerName());
            }
            PrintOrder o = new PrintOrder();
            o.setOrderDate(r.getOrderDate());
            o.setDeliveryNo(r.getDeliveryNo().trim());
            o.setPrintName(r.getPrintName().trim());
            o.setQuantity(r.getQuantity());
            o.setUnitPrice(r.getUnitPrice());
            o.setScheduleNo(r.getScheduleNo());
            o.setMaterial(r.getMaterial());
            o.setRemark(r.getRemark());
            o.setCustomerId(c.getId());
            o.setCustomerName(c.getCustomerName());
            if (StringUtils.hasText(r.getMoldModel())) {
                KnifeMold m = knifeMoldService.lambdaQuery()
                        .eq(KnifeMold::getModel, r.getMoldModel().trim())
                        .one();
                if (m != null) {
                    o.setMoldId(m.getId());
                    o.setMoldName(m.getMoldName());
                }
            }
            int ship = 0;
            if (StringUtils.hasText(r.getShippedText()) &&
                    (r.getShippedText().contains("是") || r.getShippedText().equalsIgnoreCase("1")
                            || r.getShippedText().equalsIgnoreCase("true"))) {
                ship = 1;
            }
            o.setShipped(ship);
            o.setOrderNo(BizNoUtil.orderNo());
            saveOrder(o);
        }
    }
}
