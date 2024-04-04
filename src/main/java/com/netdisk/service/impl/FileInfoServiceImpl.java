package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.enums.FileTypeEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.enums.UploadStatusEnums;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    FileInfoService fileInfoService;

    @Value("${my.outFileFolder}")
    String outFileFolder;
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
                          String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        Long useSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"useSpace",false));
        Long totalSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"totalSpace",false));
        if(StringTools.isEmpty(fileId)){
            fileId = StringTools.getRandomString(10);
        }
        //是否有同名文件
        if (fileInfoMapper.selectSameNameFile(userId,filePid,fileName) != null) {
            throw new BusinessException(ResponseCodeEnum.CODE_905);
        }
        //1.秒传
        if(chunkIndex==0) {
            //查询是否已经存在MD5值相同的文件
            FileInfo dbFile = fileInfoMapper.selectOneByMD5(fileMd5);
            if (dbFile != null) {
                //判断空间是否足够
                if (dbFile.getFileSize() + useSpace > totalSpace) {
                    throw new BusinessException(ResponseCodeEnum.CODE_904);
                }
                //秒传
                dbFile.setUserId(userId);
                dbFile.setFileId(fileId);
                dbFile.setFileName(fileName);
                dbFile.setFilePid(filePid);
                dbFile.setFileMd5(fileMd5);
                dbFile.setCreateTime(new Date());
                dbFile.setLastUpdateTime(new Date());
                fileInfoMapper.insertFileInfo(dbFile);
                //更新存储空间
                UserInfo info = new UserInfo();
                info.setUserId(userId);
                info.setUseSpace(dbFile.getFileSize() + useSpace);
                userInfoMapper.updateUserInfo(info);
                CookieTools.addCookie(response, "useSpace", String.valueOf(info.getUseSpace()), "/", true, -1);
                //处理返回结果
                Map result=new HashMap();
                result.put("fileId",fileId);
                result.put("status", UploadStatusEnums.UPLOAD_SECONDS.getCode());
                return result;
            }
        }
        //2.分片上传
        //当前分片长度是否超过总容量
        if(file.getSize()+useSpace>totalSpace){
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        CookieTools.addCookie(response,"useSpace", String.valueOf(file.getSize()+useSpace),"/",true,-1);
        //暂存临时目录
        String tempFolderName=outFileFolder+"/temp/"+userId+fileId;
        File tempFileFolder = new File(tempFolderName);
        if(!tempFileFolder.exists()){
            tempFileFolder.mkdirs();
        }
        File tempFile = new File(tempFolderName + "/" + chunkIndex);
        try {
            file.transferTo(tempFile);
        } catch (IOException e) {
            throw new BusinessException(ResponseCodeEnum.CODE_811);
        }
        if(chunkIndex+1<chunks){//不是最后一片
            //处理返回结果
            Map result=new HashMap();
            result.put("fileId",fileId);
            result.put("status",UploadStatusEnums.UPLOADING.getCode());
            return result;
        }else {
            //3.文件合并
            //根据文件后缀获取文件类型
            String fileSuffix=StringTools.getFileSuffix(fileName);
            FileTypeEnums typeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            //新增文件信息
            FileInfo info = new FileInfo();
            info.setFileId(fileId);
            info.setUserId(userId);
            info.setFileName(fileName);
            info.setFileMd5(fileMd5);
//            info.setFilePath();
//            info.setFileSize();
//            info.setFileCover();
            info.setFilePid(filePid);
            info.setCreateTime(new Date());
            info.setLastUpdateTime(new Date());
            info.setFileCategory(typeEnums.getCategory().getCategory());
            info.setFileType(typeEnums.getType());
            info.setStatus(0);//转码中
            info.setFolderType(0);//0文件 1目录
            fileInfoMapper.insertFileInfo(info);
            //更新存储空间
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setUseSpace(file.getSize() + useSpace);
            userInfoMapper.updateUserInfo(userInfo);
            //处理返回结果
            Map result=new HashMap();
            result.put("fileId",fileId);
            result.put("status",UploadStatusEnums.UPLOAD_FINISH.getCode());
            return result;
        }
    }

}
