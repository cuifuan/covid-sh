package com.cuifuan.covidsh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cuifuan.covidsh.model.Common;
import com.cuifuan.covidsh.model.ResultBean;
import com.cuifuan.covidsh.model.UrlSh;
import com.cuifuan.covidsh.model.WarnAddress;
import com.cuifuan.covidsh.service.HandleProcessor;
import com.cuifuan.covidsh.service.UrlListService;
import com.cuifuan.covidsh.service.UrlShService;
import com.cuifuan.covidsh.service.WarnAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
public class ApiController {

    @Autowired
    private UrlListService urlListService;
    @Autowired
    private UrlShService urlShService;
    @Autowired
    private HandleProcessor handleProcessor;
    @Autowired
    private WarnAddressService warnAddressService;

    @Value("${my.token}")
    private String token;


    @PostMapping("loadData")
    public String loadData(@RequestBody Map<String, String> map) {
        this.checkToken(map);
        // 查询网址
        LambdaQueryWrapper<UrlSh> urlShLqw = Wrappers.<UrlSh>lambdaQuery()
                .eq(UrlSh::getSyncStatus, 0);
        List<UrlSh> urlShList = urlShService.list(urlShLqw);
        for (UrlSh url : urlShList) {
            handleProcessor.spiderData(url.getUrl(), url.getCreateDate());
            url.setSyncStatus(1);
        }

        urlShService.updateBatchById(urlShList);

        return "success";
    }

    @GetMapping("init")
    public ResultBean<Boolean> getJobInfo() {
        return ResultBean.ok(urlListService.getGrUrl(Common.URL_WJW, false));
    }

    @PostMapping("search")
    public ResultBean<String> search(@RequestBody Map<String, String> map) {
        this.checkToken(map);
        LambdaQueryWrapper<WarnAddress> warnAddressLqw = Wrappers.<WarnAddress>lambdaQuery()
                .eq(WarnAddress::getAddress, map.get("address"));
        List<WarnAddress> warnAddressList = warnAddressService.list(warnAddressLqw);

        if (ObjectUtils.isEmpty(warnAddressList)) {
            return ResultBean.ok("地址暂未查询到感染情况.");
        } else {
            StringBuilder str = new StringBuilder();
            str.append("所属区域:<span style='color:red;'><strong>")
                    .append(warnAddressList.get(0).getRegion())
                    .append("</strong></span>,小区:<span style='color:red;'><strong>")
                    .append(warnAddressList.get(0).getAddress())
                    .append("</strong></span>.");
            DateTimeFormatter dfDateTime = DateTimeFormatter.ofPattern("MM月dd日");
            str.append("新增确诊或无症状感染者的时间分别为:");
            for (WarnAddress warnAddress : warnAddressList) {
                Instant instant = warnAddress.getConfirmDate().toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
                str.append(dfDateTime.format(localDateTime)).append(",");
            }
            String result = str.substring(0, str.length() - 1);
            result = result + ".";
            return ResultBean.ok(result);
        }
    }

    private void checkToken(Map<String, String> map) {
        if (ObjectUtils.isEmpty(map)
                || map.get("token") == null
                || !token.equals(map.get("token"))) {
            throw new RuntimeException("无权限访问~");
        }
    }
}