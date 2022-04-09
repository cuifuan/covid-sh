package com.cuifuan.covidsh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cuifuan.covidsh.model.UrlSh;
import org.apache.ibatis.annotations.Param;

public interface UrlShMapper extends BaseMapper<UrlSh> {
    /**
     * 新增或更新
     */
    int saveOrUpdate(@Param("urlData") UrlSh urlData);
}