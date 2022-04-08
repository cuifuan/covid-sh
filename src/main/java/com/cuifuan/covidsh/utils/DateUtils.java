package com.cuifuan.covidsh.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * 将长时间格式字符串转换为时间 yyyy年MM月dd日
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }
}
