package com.cuifuan.covidsh.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cuifuan.covidsh.mapper.UrlShMapper;
import com.cuifuan.covidsh.model.Common;
import com.cuifuan.covidsh.model.UrlSh;
import com.cuifuan.covidsh.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UrlListService implements PageProcessor {

    @Autowired
    private UrlShMapper urlShMapper;
    @Autowired
    private HandleProcessor handleProcessor;

    private int index = 10;
    private boolean getHistory = false;
    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private final Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    /**
     * 爬取每日感染地区数据到数据库
     */
    public boolean getGrUrl(String url, Boolean getHistory) {
        this.getHistory = getHistory;
        Spider.create(this)
                .addUrl(StringUtils.isBlank(url) ? Common.URL_WJW : url)
                .addPipeline(new ConsolePipeline()).run();
        return true;
    }

    @Override
    public void process(Page page) {
        List<Selectable> selectableList = page.getHtml().xpath("//ul[@class=\"uli16 nowrapli list-date\"]/li").nodes();
        if (!Objects.isNull(selectableList)) {
            selectableList.forEach(selectable -> {
                String title = selectable.xpath("//li/a/@title").toString();
                if (title.contains("本市各区确诊病例、无症状感染者居住地信息")) {
                    // 获取链接地址与标题
                    String url = selectable.xpath("//li/a/@href").toString();
                    if (url.contains("xwfb")) {
                        url = "https://wsjkw.sh.gov.cn" + url;
                    }
                    UrlSh urlData = new UrlSh(url, title, handleStrDate(title));
                    urlShMapper.saveOrUpdate(urlData);
                }
            });
        }
        if (index > 1 && getHistory) {
            //获取下一页的链接，将当前页数拼接到url上
            String nextUrl = "https://wsjkw.sh.gov.cn/xwfb/index_" + index + ".html";
            //将下一页链接添加到爬虫队列中
            page.addTargetRequest(nextUrl);
            index--;
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public Date handleStrDate(String strDate) {
        String tempDate = "2022年" + strDate.replaceAll(".*(.月\\d+日).*", "$1");
        return DateUtils.strToDateLong(tempDate);
    }

    /**
     * 每天上午10点抓取昨天的数据
     */
//    @Scheduled(cron = "0 00 10 * * ?")
    public void scheduled() {
        log.info("抓取昨天的数据====================================start");
        getGrUrl(Common.URL_WJW, false);
        // 查询网址
        LambdaQueryWrapper<UrlSh> urlShLqw = Wrappers.<UrlSh>lambdaQuery()
                .eq(UrlSh::getSyncStatus, 0);
        List<UrlSh> urlShList = urlShMapper.selectList(urlShLqw);
        log.info("抓到的url:{}", JSON.toJSONString(urlShList));
        if (ObjectUtils.isEmpty(urlShList)) {
            for (UrlSh urlSh : urlShList) {
                handleProcessor.spiderData(urlSh.getUrl(), urlSh.getCreateDate());
            }
        }
        log.info("抓取昨天的数据====================================end");
    }

    public static void main(String[] args) {
        // 初始数据
        new UrlListService().getGrUrl(Common.URL_WJW, true);
    }
}
