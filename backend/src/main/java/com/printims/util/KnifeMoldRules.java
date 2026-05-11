package com.printims.util;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 刀模型号与位置编码生成规则。
 */
public final class KnifeMoldRules {

    private KnifeMoldRules() {
    }

    public static String buildModel(String shapeType, BigDecimal length, BigDecimal width, BigDecimal diameter,
                                    String customSuffix) {
        if (!StringUtils.hasText(shapeType)) {
            return "";
        }
        return switch (shapeType.toUpperCase()) {
            case "RECTANGLE", "SQUARE" -> strip(length) + "*" + strip(width);
            case "CIRCLE" -> "D" + strip(diameter);
            case "CUSTOM" -> "异型-" + (StringUtils.hasText(customSuffix) ? customSuffix : "001");
            default -> strip(length) + "*" + strip(width);
        };
    }

    public static String buildLocationCode(String areaCode, String shelfNo, String layerNo, String positionNo) {
        return padPart(areaCode) + "-" + pad2(shelfNo) + "-" + pad2(layerNo) + "-" + pad2(positionNo);
    }

    private static String padPart(String s) {
        return s == null ? "" : s.trim();
    }

    private static String pad2(String s) {
        if (s == null || s.isBlank()) {
            return "00";
        }
        String t = s.trim();
        if (t.length() >= 2) {
            return t;
        }
        return "0".repeat(2 - t.length()) + t;
    }

    private static String strip(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        return v.stripTrailingZeros().toPlainString();
    }
}
