package com.cuifuan.covidsh.service;

import com.cuifuan.covidsh.mapper.WarnAddressMapper;
import com.cuifuan.covidsh.model.Common;
import com.cuifuan.covidsh.model.WarnAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@Service
public class SyncWechatShfb implements PageProcessor {

    @Resource
    private WarnAddressMapper warnAddressMapper;
    @Autowired
    private SyncWjw syncWjw;

    private Date tempDate = new Date();

    private String errorUrl = "";

    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    /**
     * 解析上海发布
     */
    public boolean handleWechat(String url, Date date) {
        this.errorUrl = url;
        tempDate = date;
        Spider.create(this)
                .addUrl(url)
                .addPipeline(new ConsolePipeline()).run();
        return true;
    }

    @Override
    public void process(Page page) {
//        List<Selectable> selectableList = page.getHtml().xpath("//section/span/strong").nodes();
//        List<String> list = new ArrayList<>(16);
//        if (ObjectUtils.isNotEmpty(selectableList)) {
//            selectableList.forEach(selectable -> {
//                String region = selectable.xpath("//strong/text()").get();
//                list.add(region);
//            });
//        }
        // 解析住址
        List<Selectable> selectableList = page.getHtml().xpath("//section/p/span").nodes();

        List<String> addressList = new ArrayList<>(4000);

//        log.info("tempList.size:{}", tempList.size());
        for (Selectable p : selectableList) {
            List<Selectable> span = p.xpath("//span").nodes();
            new SyncWjw().addressProcess(span, addressList);
        }
        String tempRegion = "";
        for (int i = 0; i < addressList.size(); i++) {
            String s = addressList.get(i);
            if (Common.REGION_LIST.contains(s)) {
                tempRegion = s;
            } else if (StringUtils.isNotBlank(s)) {
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

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        // 3-23
//        String url = "https://mp.weixin.qq.com/s/XL_hz8ESYGM8ZW7FQuHFRA";
        // 4-7
        String url = "https://mp.weixin.qq.com/s/HTM47mUp0GF-tWXkPeZJlg";
        Spider.create(new SyncWechatShfb())
                .addUrl(url)
                .addPipeline(new ConsolePipeline()).run();
    }
}
