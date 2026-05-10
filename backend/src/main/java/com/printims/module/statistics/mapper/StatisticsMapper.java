package com.printims.module.statistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计查询 Mapper。
 */
@Mapper
public interface StatisticsMapper {

    @Select("""
            SELECT COUNT(*) AS cnt FROM t_order WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            """)
    Long countOrders(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT COALESCE(SUM(amount),0) AS amt FROM t_order WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            """)
    BigDecimal sumAmount(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT
              SUM(CASE WHEN shipped = 1 THEN 1 ELSE 0 END) AS shipped,
              SUM(CASE WHEN shipped = 0 THEN 1 ELSE 0 END) AS pending
            FROM t_order WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            """)
    Map<String, Object> shippedStats(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT DATE(order_date) AS d, COUNT(*) AS c, COALESCE(SUM(amount),0) AS a
            FROM t_order WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            GROUP BY DATE(order_date) ORDER BY d
            """)
    List<Map<String, Object>> orderTrend(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT customer_name AS name, COUNT(*) AS cnt FROM t_order
            WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            GROUP BY customer_id, customer_name ORDER BY cnt DESC LIMIT 10
            """)
    List<Map<String, Object>> topCustomersByCount(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT customer_name AS name, COALESCE(SUM(amount),0) AS amt FROM t_order
            WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(order_date) <= #{end})
            GROUP BY customer_id, customer_name ORDER BY amt DESC LIMIT 10
            """)
    List<Map<String, Object>> topCustomersByAmount(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("SELECT COUNT(*) FROM t_knife_mold WHERE is_deleted = 0")
    Long countMolds();

    @Select("""
            SELECT model AS name, COUNT(*) AS cnt FROM t_knife_mold
            WHERE is_deleted = 0 GROUP BY model ORDER BY cnt DESC LIMIT 15
            """)
    List<Map<String, Object>> moldModelDistribution();

    @Select("""
            SELECT SUBSTRING_INDEX(location_code, '-', 1) AS area, COUNT(*) AS cnt
            FROM t_knife_mold WHERE is_deleted = 0
            GROUP BY SUBSTRING_INDEX(location_code, '-', 1)
            """)
    List<Map<String, Object>> moldAreaUsage();

    @Select("""
            SELECT m.mold_no AS moldNo, m.model AS model, COUNT(o.id) AS useCnt
            FROM t_knife_mold m
            LEFT JOIN t_order o ON o.mold_id = m.id AND o.is_deleted = 0
            WHERE m.is_deleted = 0
            GROUP BY m.id, m.mold_no, m.model
            ORDER BY useCnt DESC LIMIT 15
            """)
    List<Map<String, Object>> moldUseFrequency();

    @Select("SELECT COUNT(*) FROM t_customer WHERE is_deleted = 0")
    Long countCustomers();

    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m') AS m, COUNT(*) AS cnt FROM t_customer
            WHERE is_deleted = 0
            AND (#{start} IS NULL OR DATE(create_time) >= #{start})
            AND (#{end} IS NULL OR DATE(create_time) <= #{end})
            GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY m
            """)
    List<Map<String, Object>> newCustomersByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT c.customer_name AS name, COUNT(o.id) AS cnt FROM t_customer c
            LEFT JOIN t_order o ON o.customer_id = c.id AND o.is_deleted = 0
            AND (#{start} IS NULL OR DATE(o.order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(o.order_date) <= #{end})
            WHERE c.is_deleted = 0
            GROUP BY c.id, c.customer_name ORDER BY cnt DESC LIMIT 10
            """)
    List<Map<String, Object>> customerActivity(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT c.customer_name AS name, COALESCE(SUM(o.amount),0) AS amt FROM t_customer c
            LEFT JOIN t_order o ON o.customer_id = c.id AND o.is_deleted = 0
            AND (#{start} IS NULL OR DATE(o.order_date) >= #{start})
            AND (#{end} IS NULL OR DATE(o.order_date) <= #{end})
            WHERE c.is_deleted = 0
            GROUP BY c.id, c.customer_name ORDER BY amt DESC LIMIT 10
            """)
    List<Map<String, Object>> customerContribution(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
