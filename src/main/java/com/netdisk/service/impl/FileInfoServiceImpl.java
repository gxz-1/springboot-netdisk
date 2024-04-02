package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.service.FileInfoService;
import com.netdisk.vo.FileInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Override
    public PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize, String category, String userId) {
        PageHelper.startPage(pageNo, pageSize);
        FileCategoryEnums code = FileCategoryEnums.getByCode(category);
        Integer categoryNum = (code==null) ? null:code.getCategory();
        List<FileInfoVo> fileInfoVolist = fileInfoMapper.selectByUserIdAndCategory(categoryNum, userId);
        return new PageInfo<>(fileInfoVolist);
    }

}
