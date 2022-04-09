package com.cuifuan.covidsh.exception;

import com.cuifuan.covidsh.model.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResultBean<String> exceptionHandler(Exception e) {
        log.error("未知异常！原因是:" + e);
        if (ObjectUtils.isNotEmpty(e.getLocalizedMessage())) {
            return ResultBean.error(e.getLocalizedMessage());
        } else {
            return ResultBean.error(e.getMessage());
        }
    }
}
