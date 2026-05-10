package com.printims.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务单号生成工具。
 */
public final class BizNoUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private BizNoUtil() {
    }

    public static String customerNo() {
        return "CU" + LocalDateTime.now().format(FMT) + ThreadLocalRandom.current().nextInt(100, 999);
    }

    public static String moldNo() {
        return "KM" + LocalDateTime.now().format(FMT) + ThreadLocalRandom.current().nextInt(100, 999);
    }

    public static String orderNo() {
        return "PO" + LocalDateTime.now().format(FMT) + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
