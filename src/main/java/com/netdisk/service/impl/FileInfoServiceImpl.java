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
import com.netdisk.service.AsyncService;
import com.netdisk.service.FileInfoService;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.FileTools;
import com.netdisk.utils.ScaleFilter;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
@Transactional
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    AsyncService asyncService;

    @Value("${my.outFileFolder}")
    String outFileFolder;
    //分页展示分类的文件
    @Override
    public PageFileInfoVo selectPageFileInfo(Integer pageNo, Integer pageSize,
                                             String category, String userId, String filePid) {
        PageHelper.startPage(pageNo, pageSize);
        FileCategoryEnums code = FileCategoryEnums.getByCode(category);
        Integer categoryNum = (code==null) ? null:code.getCategory();//category可能为“all”,此时为null
        List<FileInfoVo> fileInfoVolist = fileInfoMapper.selectByUserIdAndCategory(categoryNum, userId,filePid);
        PageInfo<FileInfoVo> pageInfo = new PageInfo<>(fileInfoVolist);
        return new PageFileInfoVo(pageInfo.getTotal(),pageInfo.getPageSize(),pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
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
                //根据文件后缀获取文件类型
                String fileSuffix=StringTools.getFileSuffix(fileName);
                FileTypeEnums typeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
                //秒传
                dbFile.setUserId(userId);
                dbFile.setFileId(fileId);
                dbFile.setFileName(fileName);
                dbFile.setFilePid(filePid);
                dbFile.setFileMd5(fileMd5);
                dbFile.setCreateTime(new Date());
                dbFile.setLastUpdateTime(new Date());
                dbFile.setDelFlag(2);
                dbFile.setStatus(2);
                dbFile.setFileCategory(typeEnums.getCategory().getCategory());
                dbFile.setFileType(typeEnums.getType());
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
            //异步进行文件转码合并
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                //在事务提交后再执行异步方法,确保数据库的一致性
                @Override
                public void afterCommit() {
                    //通过bean的引用来调用方法时，@Async注解才会生效
                    asyncService.transferFile(info.getFileId(),info.getUserId());
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
            throw new BusinessException(ResponseCodeEnum.CODE_600);
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
        List<FileInfoVo> info = fileInfoMapper.selectFoldersByFilePid(filePid,userId);
        return info;
    }

    @Override
    public void changeFileFolder(String fileIds, String userId, String filePid) {
        //校验目录filePid是否存在,是否属于当前用户
        if(!filePid.equals("0") && fileInfoMapper.selectByUserIdAndFileId(filePid, userId, 1)==null){
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

    @Override
    public void downloadFile(HttpServletRequest request,HttpServletResponse response,String code, String userId) {
        String fileId=code;
        //校验文件是否存在
        FileInfo info = fileInfoMapper.selectByUserIdAndFileId(fileId, userId, 0);
        if(info==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        response.setContentType("application/x-msdownload; charset=UTF-8");
        String fileName=info.getFileName();
        try {
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {//IE浏览器
                    fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        FileTools.readFile(response, info.getFilePath());
    }

    //删除批量文件
    @Override
    public void deleteFile(HttpServletRequest request,HttpServletResponse response,
                           String userId, String fileIds) {
        Long useSpace= Long.valueOf(CookieTools.getCookieValue(request,null,"useSpace",false));
        //用队列存储所有待删除的文件，遇到文件夹时将目录下所有文件加入
        LinkedList<String> queue=new LinkedList<>();
        for (String fileId:fileIds.split(",")){
            queue.offer(fileId);
        }
        while (!queue.isEmpty()){
            //删除文件，即修改delFlag=1。进入回收站
            FileInfo info = fileInfoMapper.selectByUserIdAndFileId(queue.poll(),userId,null);
            if(info==null){
                continue;
            }
            //目录直接删除delFlag=0，文件放入回收站delFlag=1
            if(info.getFolderType()==1){
                fileInfoMapper.updateDelFlagByFileIdAndUserId(info.getFileId(), userId, 0,new Date());
                //如果删除的是目录，将下面的所有文件加入queue
                for (FileInfoVo vo: fileInfoMapper.selectByUserIdAndCategory(null, userId, info.getFileId())){
                    queue.offer(vo.getFileId());
                }
            }else {
                fileInfoMapper.updateDelFlagByFileIdAndUserId(info.getFileId(), userId, 1,new Date());
                useSpace-=info.getFileSize();
            }
        }
        //更新使用空间
        CookieTools.addCookie(response,"useSpace", String.valueOf(useSpace),"/",true,-1);
    }


}
