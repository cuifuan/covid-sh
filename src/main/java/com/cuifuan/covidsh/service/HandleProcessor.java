package com.cuifuan.covidsh.service;

import com.alibaba.fastjson.JSON;
import com.cuifuan.covidsh.enums.TypeEnum;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class HandleProcessor implements PageProcessor {

    @Resource
    private WarnAddressMapper warnAddressMapper;
    private Date tempDate = new Date();

    private String currentURL;

    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private final Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    /**
     * 解析上海卫健委以及上海发布数据
     */
    public void spiderData(String url, Date date) {
        this.tempDate = date;
        this.currentURL = url;
        Spider.create(this)
                .addUrl(url)
                .addPipeline(new ConsolePipeline()).run();
    }

    @Override
    public void process(Page page) {
        List<WarnAddress> warnAddressList = new ArrayList<>(1024);

        if (page.getUrl().toString().equals("https://mp.weixin.qq.com/s/xLVPnOTErTe3dmAenUyDGQ")) {
            List<String> regionList = page.getHtml().xpath("//section/strong/text()").all();
            List<Selectable> elementMent = page.getHtml().xpath("//section[@data-id='97598']").nodes();
            for (int i = 0; i < regionList.size(); i++) {
                String region = regionList.get(i);
                List<String> itemList = elementMent.get(i).xpath("//p/span/text()").all();
                itemList.stream()
                        .map(p -> p.replaceAll("，", ""))
                        .map(p -> p.replaceAll("。", ""))
                        .filter(p -> StringUtils.isNotBlank(p) && !p.contains("终末消毒") && !p.contains("无"))
                        .forEach(item -> {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//注意月份是MM
                            Date date = null;
                            try {
                                date = simpleDateFormat.parse("2022-03-18");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            warnAddressList.add(new WarnAddress(region, item, date));
                        });
            }
        } else {
            boolean isWechatChannel = page.getUrl().toString().contains("weixin");
            String xPath = isWechatChannel ? TypeEnum.WECHAT_CHANNEL_P.msg() : TypeEnum.WJW_CHANNEL_P.msg();
            String xPathChild = isWechatChannel ? TypeEnum.WECHAT_CHANNEL_C.msg() : TypeEnum.WJW_CHANNEL_C.msg();
            List<Selectable> selectableList = page.getHtml().xpath(xPath).nodes();
            // 判断使用的爬取渠道
            if (ObjectUtils.isEmpty(selectableList)) {
                xPath = isWechatChannel ? TypeEnum.WJW_CHANNEL_P.msg() : TypeEnum.WECHAT_CHANNEL_P.msg();
                xPathChild = isWechatChannel ? TypeEnum.WECHAT_CHANNEL_C.msg() : TypeEnum.WJW_CHANNEL_C.msg();
                selectableList = page.getHtml().xpath(xPath).nodes();
                if (ObjectUtils.isEmpty(selectableList)) {
                    return;
                }
            }
            // 定义感染地区集合
            List<String> addressList = new ArrayList<>(3000);

            for (Selectable element : selectableList) {
                List<Selectable> span = element.xpath(xPathChild).nodes();
                this.addressProcess(span, addressList);
//                new HandleProcessor().addressProcess(span, addressList); //本地测试用
            }
            // 定义临时地区,用来分类
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

            for (String s : addressList) {
                String[] addressArray = s.split("-");
                try {
                    WarnAddress warnAddress = new WarnAddress(addressArray[0], addressArray[1], tempDate);
                    warnAddressList.add(warnAddress);
                } catch (Exception e) {
                    log.error("addressArray数组解析出错:{}", Arrays.asList(addressArray));
                }
            }
        }
        if (ObjectUtils.isNotEmpty(warnAddressList)) {
            log.info("warnAddressList:{}", JSON.toJSONString(warnAddressList));
            warnAddressMapper.insertOrUpdate(warnAddressList);
        } else {
            log.error("错误的url为:{}", this.currentURL);
        }
    }

    private void addressProcess(List<Selectable> span, List<String> addressList) {
        StringBuilder temp = new StringBuilder();
        for (Selectable s : span) {
            temp.append(s.xpath("//span/text()").get().trim());
        }

        String address = temp.toString();

        List<String> blackList = Stream.of("终末消毒", "市卫健委", "各区信息如下",
                        "统一部署", "无", "资料", "新闻", "编辑", "滑动查看")
                .collect(Collectors.toList());
        for (String s : blackList) {
            if (address.contains(s)) {
                return;
            }
        }


        for (String s : Common.REGION_LIST) {
            if (address.contains(s)) {
                addressList.add(s);
                return;
            }
        }


        if (StringUtils.isNotBlank(address)) {

            if (address.contains("，")) {
                address = address.replaceAll("，", "");
            }

            address = address.replaceAll("。", "");

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
//        String wechatUrl = "https://mp.weixin.qq.com/s/HTM47mUp0GF-tWXkPeZJlg";
        String wechatUrl = "https://mp.weixin.qq.com/s/HTM47mUp0GF-tWXkPeZJlg";// 3-20
        String wjwUrl = "https://mp.weixin.qq.com/s/xLVPnOTErTe3dmAenUyDGQ";
        Spider.create(new HandleProcessor())
                .addUrl(wjwUrl)
                .addPipeline(new ConsolePipeline()).run();
    }
}
