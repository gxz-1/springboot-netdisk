package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.enums.ShareValidTypeEnums;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.mappers.FileShareMapper;
import com.netdisk.pojo.FileInfo;
import com.netdisk.pojo.FileShare;
import com.netdisk.service.FileInfoService;
import com.netdisk.service.FileShareService;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.JwtHelper;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class FileShareServiceImpl implements FileShareService {

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    FileShareMapper fileShareMapper;

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Autowired
    FileInfoService fileInfoService;

    @Override
    public FileShare createShare(String userId, String fileId, Integer validType, String code) {
        FileShare share = new FileShare();
        share.setUserId(userId);
        share.setFileId(fileId);
        share.setShareTime(new Date());
        //设置shareId
        share.setShareId(StringTools.getRandomString(20));
        //设置过期时间
        ShareValidTypeEnums typeEnum = ShareValidTypeEnums.getByType(validType);
        if (typeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (typeEnum != ShareValidTypeEnums.FOREVER) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, typeEnum.getDays());
            share.setExpireTime(calendar.getTime());
        }
        share.setValidType(validType);
        //设置分享码
        if (StringTools.isEmpty(code)) {
            share.setCode(StringTools.getRandomString(5));
        } else {
            share.setCode(code);
        }
        //插入数据
        this.fileShareMapper.insertFileShare(share);
        return share;
    }

    //获取已分享的文件列表
    @Override
    public PageFileInfoVo findListByPage(Integer pageNo, Integer pageSize, String userId) {
        PageHelper.startPage(pageNo, pageSize);
        List<FileShareVo> shareInfoVoList = fileShareMapper.selectPageByUserId(userId);
        PageInfo<FileShareVo> pageInfo = new PageInfo<>(shareInfoVoList);
        return new PageFileInfoVo(pageInfo.getTotal(), pageInfo.getPageSize(), pageInfo.getPageNum(), pageInfo.getPages(), pageInfo.getList());
    }

    //删除分享
    @Override
    public void deleteFileShareBatch(String[] shareIdArray, String userId) {
        Integer count = this.fileShareMapper.deleteFileShareBatch(shareIdArray, userId);
        if (count != shareIdArray.length) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    //外部获取分享的信息
    @Override
    public ShareInfoVo getShareInfoVo(HttpServletRequest request, String shareId) {
        ShareInfoVo vo = fileShareMapper.getShareInfoByShareId(shareId);
        if (vo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(vo.getExpireTime()!=null && new Date().after(vo.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断是否是当前用户分享的文件
        vo.setCurrentUser(false);//在分享主页展示“保存到我的网盘”按钮
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        if (userId != null && userId.equals(vo.getUserId())) {
            //展示“取消分享”按钮
            vo.setCurrentUser(true);
        }
        return vo;
    }

    //外部校验提取码
    @Override
    public void checkShareCode(String shareId, String code) {
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //校验提取码
        if (!fileShare.getCode().equals(code)) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        //更新浏览次数(在sql语句中增加次数，避免并发问题)
        fileShareMapper.updateShowCountByShareId(shareId);
    }

    //外部获取分享文件列表
    @Override
    public PageFileInfoVo loadDataList(Integer pageNo, Integer pageSize, String shareId, String filePid) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //开始查询
        String fileId = fileShare.getFileId();
        String userId = fileShare.getUserId();
        List<FileInfoVo> fileInfoVolist;
        //访问根目录时，展示分享的文件或目录
        if (filePid.equals("0")) {
            FileInfo info = fileInfoMapper.selectByUserIdAndFileId(fileId, userId, null);
            if (info == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            fileInfoVolist=new ArrayList<>();
            fileInfoVolist.add(new FileInfoVo(info));
            return new PageFileInfoVo(1L,15,1,1,fileInfoVolist);
        }
        //访问分享的目录时,根据filePid展示目录下的文件
        //TODO 校验filePid是分享的目录fileId下的文件
        //找filePid下所有文件
        PageHelper.startPage(pageNo, pageSize);
        fileInfoVolist = fileInfoMapper.selectByUserIdAndCategory(null, userId, filePid);
        PageInfo<FileInfoVo> pageInfo = new PageInfo<>(fileInfoVolist);
        return new PageFileInfoVo(pageInfo.getTotal(), pageInfo.getPageSize(), pageInfo.getPageNum(), pageInfo.getPages(), pageInfo.getList());
    }

    @Override
    public void saveShare(String shareId, String myUserId, String shareFileIds, String myFolderId) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        String shareUserId = fileShare.getUserId();
        //将顶层目录的文件放到myFolderId下
        for (String fileId:shareFileIds.split(",")){
            FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId, shareUserId, null);
            if(fileInfo==null){
                continue;
            }
            if(fileInfoMapper.selectSameNameFile(myUserId,myFolderId,fileInfo.getFileName(),fileInfo.getFolderType())!=null){
                throw new BusinessException(ResponseCodeEnum.CODE_905);
            }
            reCurCreate(fileInfo,myUserId,myFolderId);
        }
    }

    //递归新建文件
    void reCurCreate(FileInfo fileInfo, String myUserId, String myFolderId){
        String newFileId=StringTools.getRandomString(10);
        if(fileInfo.getFolderType()==1){
            for (FileInfo subInfo:fileInfoMapper.selectListByUserIdAndFilePid(fileInfo.getUserId(), fileInfo.getFileId())){
                reCurCreate(subInfo,myUserId,newFileId);
            }
        }
        fileInfo.setFileId(newFileId);
        fileInfo.setUserId(myUserId);
        fileInfo.setFilePid(myFolderId);
        fileInfo.setCreateTime(new Date());
        fileInfo.setLastUpdateTime(new Date());
        fileInfoMapper.insertFileInfo(fileInfo);
    }

    @Override
    public List<FileInfoVo> getFolderInfo(String shareId, String path) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        String userId = fileShare.getUserId();
        return fileInfoService.getFolderInfo(path,userId);
    }

    @Override
    public void getFileInfo(HttpServletResponse response,String shareId, String fileId) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        String userId = fileShare.getUserId();
        fileInfoService.getFileInfo(response,fileId,userId);
    }

    @Override
    public void getVideoInfo(HttpServletResponse response, String shareId, String fileId) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        String userId = fileShare.getUserId();
        fileInfoService.getVideoInfo(response,fileId,userId);
    }

    @Override
    public String createDownloadToken(String shareId,String fileId) {
        //校验shareId
        if (StringTools.isEmpty(shareId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断分享链接是否失效,null表示永久有效
        if(fileShare.getExpireTime()!=null && new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        String userId = fileShare.getUserId();
        return fileInfoService.createDownloadToken(fileId,userId);
    }

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String downloadToken) {
        fileInfoService.downloadFile(request,response,downloadToken);
    }


}
