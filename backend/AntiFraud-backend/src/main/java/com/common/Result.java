package com.common;

import com.common.enums.ResultCode;
import lombok.Data;

@Data
public class Result<T> {
    private String message;
    private String code;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error() {
        return error(ResultCode.SYSTEM_ERROR);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SYSTEM_ERROR.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        return result;
    }
}
