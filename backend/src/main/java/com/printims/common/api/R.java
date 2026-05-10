package com.printims.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 返回结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private int code;
    private String msg;
    private T data;
    private long timestamp;

    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(200, msg, data, System.currentTimeMillis());
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null, System.currentTimeMillis());
    }
}
