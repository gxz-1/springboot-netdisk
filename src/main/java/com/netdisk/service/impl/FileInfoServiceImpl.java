package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.mappers.UserInfoMapper;
import com.netdisk.pojo.FileInfo;
import com.netdisk.pojo.UserInfo;
import com.netdisk.service.FileInfoService;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.FileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;
    UserInfoMapper userInfoMapper;
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
    public Map uploadFile(HttpServletRequest request, HttpServletResponse response,
                          String userId, String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks) {
        Long useSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"useSpace",false));
        Long totalSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"totalSpace",false));
        if(StringTools.isEmpty(fileId)){
            fileId = StringTools.getRandomNumber(10);
        }
        //对于第一个分片
        if(chunkIndex==0){
            //查询是否已经存在MD5值相同的文件
            FileInfo dbFile = fileInfoMapper.selectOneByMD5(fileMd5);
            if(dbFile!=null){
                //判断空间是否足够
                if(dbFile.getFileSize()+useSpace > totalSpace){
                    throw new BusinessException(ResponseCodeEnum.CODE_904);
                }
                //是否有同名文件
                if(fileInfoMapper.selectOneByUserIdAndfilePidAndfileName()!=null){
                    throw new BusinessException(ResponseCodeEnum.CODE_905);
                }
                //更新存储空间
                UserInfo info = new UserInfo();
                info.setUserId(userId);
                info.setUseSpace(dbFile.getFileSize()+useSpace);
                userInfoMapper.updateUserInfo(info);
                CookieTools.addCookie(response,"useSpace", String.valueOf(info.getUseSpace()),"/",true,-1);
                //秒传
                chunkIndex=chunks;
                dbFile.setUserId(userId);
                dbFile.setFileId(fileId);
                dbFile.setFileName(fileName);
                dbFile.setFilePid(filePid);
                dbFile.setFileMd5(fileMd5);
                dbFile.setCreateTime(new Date());
                dbFile.setLastUpdateTime(new Date());
                fileInfoMapper.updateFileInfo(dbFile);
            }
            //正常上传
        }

        //处理返回结果
        Map result=new HashMap();
        result.put("fileId",fileId);
        result.put("status",chunkIndex == chunks ? "上传成功" : "上传中");
        return null;
    }

}
