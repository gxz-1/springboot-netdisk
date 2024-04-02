package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.service.FileInfoService;
import com.netdisk.vo.FileInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;

    //分页展示分类的文件
    @Override
    public PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize, String category, String userId) {
        PageHelper.startPage(pageNo, pageSize);
        FileCategoryEnums code = FileCategoryEnums.getByCode(category);
        Integer categoryNum = (code==null) ? null:code.getCategory();//category可能为“all”,此时为null
        List<FileInfoVo> fileInfoVolist = fileInfoMapper.selectByUserIdAndCategory(categoryNum, userId);
        return new PageInfo<>(fileInfoVolist);
    }

    //文件上传
    @Override
    public Map uploadFile(String userId, String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks) {

        //处理返回结果
        Map result=new HashMap();
        result.put("fileId",0);
        result.put("status",0);
        return null;
    }

}
