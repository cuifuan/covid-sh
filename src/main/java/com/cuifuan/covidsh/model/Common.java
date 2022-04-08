package com.cuifuan.covidsh.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Common {
    public final static List<String> REGION_LIST = Stream
            .of("浦东新区", "闵行区", "静安区", "黄浦区", "松江区", "青浦区", "嘉定区",
                    "奉贤区", "徐汇区", "宝山区", "虹口区", "长宁区", "崇明区", "杨浦区", "金山区", "普陀区")
            .collect(Collectors.toList());
}
