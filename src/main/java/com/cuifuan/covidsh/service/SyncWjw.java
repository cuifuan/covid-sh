package com.cuifuan.covidsh.service;

import com.cuifuan.covidsh.mapper.WarnAddressMapper;
import com.cuifuan.covidsh.model.Common;
import com.cuifuan.covidsh.model.WarnAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SyncWjw implements PageProcessor {

    @Resource
    private WarnAddressMapper warnAddressMapper;
    private Date tempDate = new Date();

    private String errorUrl = "";

    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    /**
     * 解析上海卫健委发布
     */
    public boolean handleWjw(String url, Date date) {
        this.tempDate = date;
        this.errorUrl = url;
        Spider.create(this)
                .addUrl(url)
                .addPipeline(new ConsolePipeline()).run();
        return true;
    }

    @Override
    public void process(Page page) {
        List<Selectable> selectableList = page.getHtml().xpath("//div[@id='ivs_content']/p").nodes();
        List<String> addressList = new ArrayList<>(4000);

        for (Selectable p : selectableList) {
            List<Selectable> span = p.xpath("//p//span").nodes();
            this.addressProcess(span, addressList);
        }
        String tempRegion = "";
        for (int i = 0; i < addressList.size(); i++) {
            String s = addressList.get(i);
            if (Common.REGION_LIST.contains(s)) {
                tempRegion = s;
            } else {
                addressList.set(i, tempRegion + "-" + s);
            }
        }
        addressList.removeAll(Common.REGION_LIST);
        List<WarnAddress> warnAddressList = new ArrayList<>(1024);

        for (String s : addressList) {
            String[] addressArray = s.split("-");
            try {
                WarnAddress warnAddress = new WarnAddress(addressArray[0], addressArray[1], tempDate);
                warnAddressList.add(warnAddress);
            } catch (Exception e) {
                log.error("addressArray数组解析出错:{}", Arrays.asList(addressArray));
            }
        }
        if (ObjectUtils.isNotEmpty(warnAddressList)) {
            warnAddressMapper.insertOrUpdate(warnAddressList);
        } else {
            log.error("错误的url为:{}", this.errorUrl);
        }
    }

    public void addressProcess(List<Selectable> span, List<String> addressList) {
        StringBuilder temp = new StringBuilder();
        for (Selectable s : span) {
            temp.append(s.xpath("//span/text()").get().trim());
        }
        String address = temp.toString();
        for (String s : Common.REGION_LIST) {
            if (address.contains(s)) {
                addressList.add(s);
                return;
            }
        }
        List<String> blackList = Stream.of("终末消毒", "市卫健委", "各区信息如下",
                        "统一部署", "无", "资料", "新闻", "编辑", "滑动查看")
                .collect(Collectors.toList());
        for (String s : blackList) {
            if (address.contains(s)) {
                return;
            }
        }

        if (StringUtils.isNotBlank(address)) {

            if (address.length() < 2) {
                log.error("errorStr:{}", address);
            } else {
                address = address.substring(0, address.length() - 1);
            }

            if (address.contains("，")) {
                address = address.replaceAll("，", "");
            }
            if (address.contains("、")) {
                String[] addressArray = address.split("、");
                List<String> tempList = Stream.of(addressArray)
                        .map(String::trim)
                        .collect(Collectors.toList());
                addressList.addAll(tempList);
            } else {
                addressList.add(address);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new SyncWjw())
                .addUrl("https://wsjkw.sh.gov.cn/xwfb/20220319/dc5938b3d12d4d86be7470ae03beac1c.html")
                .addPipeline(new ConsolePipeline()).run();
    }
}
