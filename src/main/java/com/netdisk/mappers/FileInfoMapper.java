package com.netdisk.mappers;

import com.netdisk.vo.FileInfoVo;

import java.util.List;

public interface FileInfoMapper {
    List<FileInfoVo> selectByUserIdAndCategory(Integer category, String userId);
}

