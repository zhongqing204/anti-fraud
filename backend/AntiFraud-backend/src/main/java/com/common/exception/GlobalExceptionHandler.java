package com.common.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.common.Result;
import com.common.enums.ResultCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice("com.controller")
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.get();

    @ExceptionHandler(Exception.class)
    @ResponseBody // 返回json串
    public Result error(Exception e) {
        log.error("异常信息：", e);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(),
                ResultCode.SYSTEM_ERROR.getMessage() + ": " + e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody // 返回json串
    public Result error(CustomException e) {
        log.info("业务异常：" + e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}
