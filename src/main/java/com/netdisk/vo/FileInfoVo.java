package com.netdisk.vo;

import lombok.Data;
import java.util.Date;

@Data
public class FileInfoVo {
    private String fileId;
    private String filePid;
    private Long fileSize;
    private String fileName;
    private String fileCover;
    private Date createTime;
    private Date lastUpdateTime;
    private Integer folderType;
    private Integer fileCategory;
    private Integer fileType;
    private Integer status;
}
