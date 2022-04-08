package com.cuifuan.covidsh.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "url_sh")
public class UrlSh {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "url")
    private String url;

    @TableField(value = "title")
    private String title;

    @TableField(value = "create_date")
    private Date createDate;

    /**
     * 0-未同步 1-已同步
     */
    private Integer syncStatus;

    public static final String COL_ID = "id";

    public static final String COL_URL = "url";

    public static final String COL_TITLE = "title";

    public static final String COL_CREATEDATE = "create_date";

    public UrlSh(String url, String title, Date createDate) {
        this.url = url;
        this.title = title;
        this.createDate = createDate;
    }
}