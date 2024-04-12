package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.pojo.FileInfo;
import com.netdisk.service.RecycleService;
import com.netdisk.utils.CookieTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class RecycleServiceImpl implements RecycleService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public PageFileInfoVo selectPageFileInfo(Integer pageNo, Integer pageSize, String userId) {
        PageHelper.startPage(pageNo, pageSize);
        List<FileInfoVo> fileInfoVolist = fileInfoMapper.selectDelFileList(userId);
        PageInfo<FileInfoVo> pageInfo = new PageInfo<>(fileInfoVolist);
        return new PageFileInfoVo(pageInfo.getTotal(),pageInfo.getPageSize(),pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }

    @Override
    public void recoverFile(HttpServletRequest request, HttpServletResponse response,
                            String fileIds, String userId) {
        Long totalSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"totalSpace",false));
        Long useSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"useSpace",false));
        for(String fileId : fileIds.split(",")){
            FileInfo info = fileInfoMapper.selectDelFileByUserIdAndFileId(fileId, userId);
            if(info==null || info.getFolderType()==1){
                continue;
            }
            //根目录下是否有同名文件
            if (fileInfoMapper.selectSameNameFile(userId,"0",info.getFileName(),0) != null) {
                throw new BusinessException(ResponseCodeEnum.CODE_905);
            }
            //判断空间是否足够
            if(useSpace+info.getFileSize()>totalSpace){
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }
            //还原文件到根目录
            useSpace+=info.getFileSize();
            fileInfoMapper.updateDelFlagByFileIdAndUserId(fileId,userId,2,new Date());
        }
        //更新使用空间
        CookieTools.addCookie(response,"useSpace", String.valueOf(useSpace),"/",true,-1);
    }

    @Override
    public void delFile(String fileIds, String userId) {
        for(String fileId : fileIds.split(",")){
            fileInfoMapper.updateDelFlagByFileIdAndUserId(fileId,userId,0,new Date());
        }
    }
}
