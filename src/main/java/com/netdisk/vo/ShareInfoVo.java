package com.netdisk.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShareInfoVo {
    //从FileShare表中拿
    private String fileId;
    private String userId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    //从UserInfo中拿
    private String nickName;
    //从FileInfo中拿
    private String fileName;
    //后端设置
    private Boolean currentUser;
}
