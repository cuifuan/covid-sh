package com.cuifuan.covidsh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cuifuan.covidsh.model.WarnAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WarnAddressMapper extends BaseMapper<WarnAddress> {

    int insertOrUpdate(@Param("list") List<WarnAddress> warnAddressList);
}