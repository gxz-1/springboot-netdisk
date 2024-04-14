package com.netdisk.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageFileInfoVo<T> {
    private Long totalCount;
    private Integer pageSize;
    private Integer pageNo;
    private Integer pageTotal;
    private List<T> list;

    public PageFileInfoVo(Long totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<T> pageInfo){
        this.totalCount=totalCount;
        this.pageSize=pageSize;
        this.pageNo=pageNo;
        this.pageTotal=pageTotal;
        this.list=pageInfo;
    }

}
