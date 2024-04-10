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
import com.netdisk.utils.*;
import com.netdisk.vo.FileInfoVo;
import jakarta.mail.Folder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.mockito.invocation.StubInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Service
@Transactional
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    @Lazy// 避免循环依赖
    FileInfoService fileInfoService;//需要fileInfoService调用transferFile()进行异步合并文件

    @Value("${my.outFileFolder}")
    String outFileFolder;
    //分页展示分类的文件
    @Override
    public PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize,
                                                   String category, String userId,String filePid) {
        PageHelper.startPage(pageNo, pageSize);
        FileCategoryEnums code = FileCategoryEnums.getByCode(category);
        Integer categoryNum = (code==null) ? null:code.getCategory();//category可能为“all”,此时为null
        List<FileInfoVo> fileInfoVolist = fileInfoMapper.selectByUserIdAndCategory(categoryNum, userId,filePid);
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
        if (fileInfoMapper.selectSameNameFile(userId,filePid,fileName,0) != null) {
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
            info.setFilePath(outFileFolder+"/file/"+userId+fileId+fileSuffix);//定义服务器文件存储路径
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
            //TODO 异步操作：进行文件合并 @Lazy？
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                //需要在事务提交后再进行
                public void afterCommit() {
                    fileInfoService.transferFile(info.getFileId(),info.getUserId());//由Spring管理，使用fileInfoService调用才能使异步生效
                }
            });
            //处理返回结果
            Map result=new HashMap();
            result.put("fileId",fileId);
            result.put("status",UploadStatusEnums.UPLOAD_FINISH.getCode());
            return result;
        }
    }

    @Override
    @Async
    public void transferFile(String fileId,String userId) {
        Boolean isSuccess = true;
        String coverName = null;
        FileTypeEnums typeEnums = null;
        FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId, userId,0);
        if (fileInfo == null || fileInfo.getStatus() != 0) {
            return;//没找到或者文件不在转码中,不处理
        }
        //临时目录
        File tempDirPath = new File(outFileFolder + "/temp/" + userId + fileId);
        File targetFile = new File(fileInfo.getFilePath());
        if (!tempDirPath.exists()) {
            return;//临时文件不存在了
        }
        //开始合并
        try {
            RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < tempDirPath.listFiles().length; ++i) {
                int len = -1;
                //创建读块文件的对象
                File chunkFile = new File(tempDirPath.getPath() + File.separator + i);
                RandomAccessFile readFile = new RandomAccessFile(chunkFile, "r");
                while( (len = readFile.read(b)) != -1) {
                    writeFile.write(b, 0, len);
                }
                readFile.close();
            }
            writeFile.close();
            if (tempDirPath.exists()) {//删除临时文件
                FileUtils.deleteDirectory(tempDirPath);
            }
        } catch (Exception e) {
            isSuccess=false;
            throw new BusinessException("合并文件失败");
        }
        //视频切割，生成缩略图
        typeEnums=FileTypeEnums.getByType(fileInfo.getFileType());

        if(typeEnums == FileTypeEnums.VIDEO){
            //视频文件切割
            FileTools.cutVideo(fileId,fileInfo.getFilePath());
            //生成缩略图
            coverName=userId+fileId+".png";
            ScaleFilter.createCover4Video(new File(fileInfo.getFilePath()),150,new File(outFileFolder+"/file/"+coverName));
        }else if(typeEnums == FileTypeEnums.IMAGE){//图片
            coverName=userId+fileId+"_.png";//多加一个_区分缩略图和原图
            Boolean ok = ScaleFilter.createThumbnailWidthFFmpeg(new File(fileInfo.getFilePath()), 150,
                    new File(outFileFolder+"/file/"+coverName), false);
            if(!ok){//压缩失败，例如原图太小
                try {
                    FileUtils.copyFile(new File(fileInfo.getFilePath()),new File(outFileFolder+"/file/"+coverName));
                } catch (IOException e) {
                    throw new BusinessException("复制图片失败");
                }
            }
        }
        //设置文件大小
        fileInfo.setFileSize(targetFile.length());
        fileInfo.setFileCover(coverName);
        fileInfo.setStatus(isSuccess?2:1);//2转码成功 1转码失败
        fileInfoMapper.updateFileInfo(fileInfo); //TODO 可能存在写后写问题
    }

    @Override
    public void getVideoInfo(HttpServletResponse response,String fileId,String userId) {
        String filePath;
        if(fileId.endsWith(".ts")){
            //读取视频分片
            String realFileId=fileId.split("_")[0];
            FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(realFileId,userId,0);
            String absPath = fileInfo.getFilePath();
            filePath=absPath.substring(0, absPath.lastIndexOf("."))+"/"+fileId;
        }else {
            FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId,userId,0);
            String absPath = fileInfo.getFilePath();
            //读取m3u8文件
            filePath = absPath.substring(0, absPath.lastIndexOf("."))+"/index.m3u8";
        }
        FileTools.readFile(response,filePath);
    }

    @Override
    public void getFileInfo(HttpServletResponse response, String fileId, String userId) {
        FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId, userId,0);
        String filePath=fileInfo.getFilePath();
        FileTools.readFile(response,filePath);
    }

    @Override
    public FileInfoVo createFolder(String filePid, String userId, String fileName) {
        //是否有同名目录
        if (fileInfoMapper.selectSameNameFile(userId,filePid,fileName,1) != null) {
            throw new BusinessException(ResponseCodeEnum.CODE_905);
        }
        FileInfo info=new FileInfo();
        info.setFileId(StringTools.getRandomString(10));
        info.setUserId(userId);
        info.setFilePid(filePid);
        info.setFileName(fileName);
        info.setFolderType(1);//0文件 1目录
        info.setCreateTime(new Date());
        info.setLastUpdateTime(new Date());
        fileInfoMapper.insertFileInfo(info);
        return new FileInfoVo(info);
    }

    @Override
    public List<FileInfoVo> getFolderInfo(String path, String userId) {
        List<FileInfoVo> res=new ArrayList<>();
        for(String fileId : path.split("/")){
            FileInfoVo vo=new FileInfoVo(fileInfoMapper.selectByUserIdAndFileId(fileId, userId,1));
            res.add(vo);
        }
        return res;
    }

    @Override
    public FileInfoVo fileRename(String fileId, String userId, String fileName) {
        FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId,userId,null);
        //文件是否存在
        if(fileInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_906);
        }
        Integer folderType = fileInfo.getFolderType();
        //对于文件要加上原来的后缀名
        if(folderType==0){
            String originFileName = fileInfo.getFileName();
            fileName+=StringTools.getFileSuffix(originFileName);
        }
        //是否有同名文件或目录
        if (fileInfoMapper.selectSameNameFile(userId,fileInfo.getFilePid(),fileName,folderType) != null) {
            throw new BusinessException(ResponseCodeEnum.CODE_905);
        }
        //完成重命名
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(new Date());
        fileInfoMapper.updateFileInfo(fileInfo);
        return new FileInfoVo(fileInfo);
    }

    @Override
    public List<FileInfoVo> loadAllFolder(String userId, String filePid, String currentFileIds) {
        String[] fileIdList=null;
        //TODO 不需要currentFileIds
//        if(!StringTools.isEmpty(currentFileIds)){
//            fileIdList = currentFileIds.split(",");
//        }
        List<FileInfoVo> info = fileInfoMapper.selectFoldersByFilePid(filePid,userId,fileIdList);
        return info;
    }

    @Override
    public void changeFileFolder(String fileIds, String userId, String filePid) {
        //校验filePid是否存在,是否属于当前用户
        if(fileInfoMapper.selectByUserIdAndFileId(filePid, userId, 1)==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        for (String fileId:fileIds.split(",")){
            //不能将自己移动到自己下面
            if(fileId.equals(filePid)){
                continue;
            }
            FileInfo info = fileInfoMapper.selectByUserIdAndFileId(fileId, userId, null);
            //移动后的目录下是否有同名的文件或目录
            if (fileInfoMapper.selectSameNameFile(userId,filePid,info.getFileName(),info.getFolderType()) != null) {
                throw new BusinessException(ResponseCodeEnum.CODE_905);
            }
            //移动文件，即修改pid
            info.setFilePid(filePid);
            info.setLastUpdateTime(new Date());
            fileInfoMapper.updateFileInfo(info);
        }
    }


}
