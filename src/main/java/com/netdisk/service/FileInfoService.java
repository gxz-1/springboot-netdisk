package com.netdisk.service;

import com.github.pagehelper.PageInfo;
import com.netdisk.vo.FileInfoVo;

public interface FileInfoService {
    PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize, String category, String userId);
}
