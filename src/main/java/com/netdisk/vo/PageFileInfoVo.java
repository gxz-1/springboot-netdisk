package com.netdisk.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageFileInfoVo {
    private Long totalCount;
    private Integer pageSize;
    private Integer pageNo;
    private Integer pageTotal;
    private List<FileInfoVo> list;

    public PageFileInfoVo(Long totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<FileInfoVo> pageInfo){
        this.totalCount=totalCount;
        this.pageSize=pageSize;
        this.pageNo=pageNo;
        this.pageTotal=pageTotal;
        this.list=pageInfo;
    }
}
