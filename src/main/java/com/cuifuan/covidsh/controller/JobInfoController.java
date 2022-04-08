package com.cuifuan.covidsh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cuifuan.covidsh.model.UrlSh;
import com.cuifuan.covidsh.service.ShyqProcessor;
import com.cuifuan.covidsh.service.SyncWechatShfb;
import com.cuifuan.covidsh.service.SyncWjw;
import com.cuifuan.covidsh.service.UrlShService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JobInfoController {

    @Autowired
    private ShyqProcessor shyqProcessor;
    @Autowired
    private UrlShService urlShService;
    @Autowired
    private SyncWechatShfb wechat;
    @Autowired
    private SyncWjw wjw;

    @PostMapping("loadData")
    public String loadData(@RequestBody Map<String, String> map) {
        if (ObjectUtils.isEmpty(map)
                || map.get("token") == null
                || !"cuifuan".equals(map.get("token"))) {
            return "无权限访问;";
        }
        // 查询网址
        LambdaQueryWrapper<UrlSh> urlShLqw = Wrappers.<UrlSh>lambdaQuery()
                .eq(UrlSh::getSyncStatus, 0);
        List<UrlSh> urlShList = urlShService.list(urlShLqw);
        for (UrlSh url : urlShList) {
            if (url.getUrl().contains("weixin")) {
                wechat.handleWechat(url.getUrl(), url.getCreateDate());
            } else {
                wjw.handleWjw(url.getUrl(), url.getCreateDate());
            }
            url.setSyncStatus(1);
        }

        urlShService.updateBatchById(urlShList);

        return "success";
    }

    @GetMapping("/getJobInfo")
    public String getJobInfo() {
        shyqProcessor.getGrUrl(null, false);
        return "success";
    }
}