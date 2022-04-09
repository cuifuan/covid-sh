package com.cuifuan.covidsh.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ResultBean<T> implements Serializable {

    public static final int SUCCESS = 0;
    public static final int FAIL = -1;
    private static final long serialVersionUID = 1L;
    private String msg = "success";

    private int code = SUCCESS;

    private T data;

    public ResultBean(T data) {
        super();
        this.data = data;
    }

    public ResultBean(Throwable e) {
        super();
        this.msg = e.toString();
        this.code = FAIL;
    }

    public static <T> ResultBean<T> ok(T data) {
        return new ResultBean<>(data);
    }

    public static ResultBean<String> error(String errorMsg) {
        ResultBean<String> result = new ResultBean<>();
        result.setCode(ResultBean.FAIL);
        result.setMsg(errorMsg);
        return result;
    }

    public ResultBean<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ResultBean<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public ResultBean<T> setData(T data) {
        this.data = data;
        return this;
    }
}