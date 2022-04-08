package com.cuifuan.covidsh.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cuifuan.covidsh.mapper.WarnAddressMapper;
import com.cuifuan.covidsh.model.WarnAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WarnAddressService extends ServiceImpl<WarnAddressMapper, WarnAddress> {

    public static void main(String[] args) {
//        String str = "2022年4月3日，黄浦区新增14例本土确诊病例，新增810例本土无症状感染者，分别居住于：";
//        System.out.println(str.replaceAll(".*日，([\\u2E80-\\u9FFF]+)区.*","$1区"));

//        List<String> regionList = Stream.of("浦东新区", "青浦区").collect(Collectors.toList());
//
//        String[] strings = new String[]{
//                "浦东新区",
//                "艾东村薛家宅",
//                "八灶村周家宅",
//                "板泉路1201弄",
//                "青浦区",
//                "白鹤村",
//                "北青公路6972弄",
//                "漕泾村",
//                "陈东村",
//                "陈桥村",
//                "城北新村",
//        };
//
//        List<String> addressList = Stream.of(strings).collect(Collectors.toList());
//
//        String tempRegion = "";
//        for (int i = 0; i < addressList.size(); i++) {
//            String s = addressList.get(i);
//            if (regionList.contains(s)) {
//                tempRegion = s;
//            } else {
//                addressList.set(i, tempRegion + "-" + s);
//            }
//        }
//
//        log.info("给地址增加区域显示:{}", JSON.toJSONString(addressList));
        String str ="XXXXX.";
        System.out.println(str.substring(0,str.length() - 1));
    }
}
