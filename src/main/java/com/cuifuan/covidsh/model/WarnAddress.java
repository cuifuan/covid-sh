package com.cuifuan.covidsh.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "warn_address")
public class WarnAddress {
    @TableId(value = "warn_id", type = IdType.AUTO)
    private Integer warnId;

    @TableField(value = "region")
    private String region;

    @TableField(value = "address")
    private String address;

    @TableField(value = "confirm_date")
    private Date confirmDate;

    public static final String COL_WARN_ID = "warn_id";

    public static final String COL_REGION = "region";

    public static final String COL_ADDRESS = "address";

    public static final String COL_CONFIRM_DATE = "confirm_date";

    public WarnAddress(String region, String address, Date confirmDate) {
        this.region = region;
        this.address = address;
        this.confirmDate = confirmDate;
    }
}