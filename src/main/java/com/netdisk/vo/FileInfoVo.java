package com.netdisk.vo;

import com.netdisk.pojo.FileInfo;
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

    //TODO mapper需要提供无参构造才能自动匹配
    public FileInfoVo(){}

    public FileInfoVo(FileInfo info){
        fileId=info.getFileId();
        filePid=info.getFilePid();
        fileSize=info.getFileSize();
        fileName=info.getFileName();
        fileCover=info.getFileCover();
        createTime=info.getCreateTime();
        lastUpdateTime=info.getLastUpdateTime();
        folderType=info.getFolderType();
        fileCategory=info.getFileCategory();
        fileType=info.getFileType();
        status=info.getStatus();
    }
}
