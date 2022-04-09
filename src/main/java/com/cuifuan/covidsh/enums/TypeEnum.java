package com.cuifuan.covidsh.enums;

import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public enum TypeEnum {

    WECHAT_CHANNEL_P(1,"//section/p/span"),
    WJW_CHANNEL_P(2,"//div[@id='ivs_content']/p"),
    WECHAT_CHANNEL_C(3,"//span"),
    WJW_CHANNEL_C(4,"//p//span");

    private Integer code;
    private String message;

    public Integer code() {
        return this.code;
    }

    public String msg() {
        return this.message;
    }
}
