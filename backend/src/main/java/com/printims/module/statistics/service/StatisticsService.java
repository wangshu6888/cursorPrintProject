package com.printims.module.statistics.service;

import com.printims.module.statistics.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘与统计汇总。
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    /**
     * 汇总看板所需全部序列数据。
     */
    public Map<String, Object> dashboard(LocalDate start, LocalDate end) {
        Map<String, Object> m = new HashMap<>();
        m.put("orderTotal", statisticsMapper.countOrders(start, end));
        m.put("orderAmount", statisticsMapper.sumAmount(start, end));
        m.put("shipped", statisticsMapper.shippedStats(start, end));
        m.put("orderTrend", statisticsMapper.orderTrend(start, end));
        m.put("topCustomersByCount", statisticsMapper.topCustomersByCount(start, end));
        m.put("topCustomersByAmount", statisticsMapper.topCustomersByAmount(start, end));
        m.put("moldTotal", statisticsMapper.countMolds());
        m.put("moldModelDistribution", statisticsMapper.moldModelDistribution());
        m.put("moldAreaUsage", statisticsMapper.moldAreaUsage());
        m.put("moldUseFrequency", statisticsMapper.moldUseFrequency());
        m.put("customerTotal", statisticsMapper.countCustomers());
        m.put("newCustomersByMonth", statisticsMapper.newCustomersByMonth(start, end));
        m.put("customerActivity", statisticsMapper.customerActivity(start, end));
        m.put("customerContribution", statisticsMapper.customerContribution(start, end));
        return m;
    }
}
