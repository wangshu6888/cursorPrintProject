package com.printims.module.business.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.common.api.PageResult;
import com.printims.common.exception.BusinessException;
import com.printims.module.business.dto.CustomerQuery;
import com.printims.module.business.entity.Customer;
import com.printims.module.business.excel.CustomerExcelRow;
import com.printims.module.business.mapper.CustomerMapper;
import com.printims.module.business.service.CustomerService;
import com.printims.util.BizNoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 客户服务实现。
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public PageResult<Customer> pageQuery(CustomerQuery query) {
        Page<Customer> p = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String k = query.getKeyword().trim();
            w.and(x -> x.like(Customer::getCustomerName, k)
                    .or().like(Customer::getPhone, k)
                    .or().like(Customer::getCustomerNo, k));
        }
        w.orderByDesc(Customer::getCreateTime);
        Page<Customer> result = page(p, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @CacheEvict(value = "dashboard", allEntries = true)
    public void saveCustomer(Customer entity) {
        if (!StringUtils.hasText(entity.getCustomerName())) {
            throw new BusinessException("客户名称不能为空");
        }
        if (!StringUtils.hasText(entity.getCustomerNo())) {
            entity.setCustomerNo(BizNoUtil.customerNo());
        }
        saveOrUpdate(entity);
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<Customer> list = list();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("客户导出", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<CustomerExcelRow> rows = list.stream().map(c -> {
            CustomerExcelRow r = new CustomerExcelRow();
            r.setCustomerName(c.getCustomerName());
            r.setContactPerson(c.getContactPerson());
            r.setPhone(c.getPhone());
            r.setAddress(c.getAddress());
            r.setEmail(c.getEmail());
            r.setRemark(c.getRemark());
            return r;
        }).toList();
        EasyExcel.write(response.getOutputStream(), CustomerExcelRow.class).sheet("客户").doWrite(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) throws IOException {
        List<CustomerExcelRow> rows = EasyExcel.read(file.getInputStream()).head(CustomerExcelRow.class).sheet().doReadSync();
        for (CustomerExcelRow r : rows) {
            if (!StringUtils.hasText(r.getCustomerName()) || !StringUtils.hasText(r.getPhone())) {
                continue;
            }
            Customer c = new Customer();
            c.setCustomerNo(BizNoUtil.customerNo());
            c.setCustomerName(r.getCustomerName().trim());
            c.setContactPerson(r.getContactPerson());
            c.setPhone(r.getPhone().trim());
            c.setAddress(r.getAddress());
            c.setEmail(r.getEmail());
            c.setRemark(r.getRemark());
            long cnt = lambdaQuery().eq(Customer::getCustomerName, c.getCustomerName()).count();
            if (cnt > 0) {
                throw new BusinessException("导入失败：客户名称已存在 " + c.getCustomerName());
            }
            save(c);
        }
    }
}
