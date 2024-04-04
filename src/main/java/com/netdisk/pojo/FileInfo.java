package com.netdisk.pojo;


import lombok.Data;

import java.util.Date;

@Data
public class FileInfo {
    private String fileId; // 文件id
    private String userId; // 用户id
    private String fileMd5; // md5,用于实现秒传
    private String filePid; // 父级ID，用于实现目录
    private Long fileSize; // 文件大小
    private String fileName; // 文件名称
    private String fileCover; // 文件预览封面
    private String filePath; // 文件在服务器上的路径
    private Date createTime; // 文件创建时间
    private Date lastUpdateTime; // 文件更新时间
    private Integer folderType; // 0文件 1目录
    private Integer fileCategory; // 文件类别：1视频 2音乐 3图片 4文档 5其他
    private Integer fileType; // 详细类别：1视频 2音乐 3图片 4pdf 5doc 6excel 7txt 8code 9zip 10其他
    private Integer status; // 0转码中 1转码失败 2转码成功
    private Date recoveryTime; // 进入回收站时间
    private Integer delFlag; // 0删除 1回收站 2正常

}
