package com.netdisk.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.enums.ShareValidTypeEnums;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.mappers.FileShareMapper;
import com.netdisk.pojo.FileInfo;
import com.netdisk.pojo.FileShare;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class FileShareServiceImpl implements FileShareService {

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    FileShareMapper fileShareMapper;

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Override
    public FileShare saveShare(String userId, String fileId, Integer validType, String code) {
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
        //设置分享码
        if (StringTools.isEmpty(code)) {
            share.setCode(StringTools.getRandomString(5));
        }else{
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
        List<FileShareVo> shareInfoVoList = fileShareMapper.selectPageByUserId();
        PageInfo<FileShareVo> pageInfo = new PageInfo<>(shareInfoVoList);
        return new PageFileInfoVo(pageInfo.getTotal(),pageInfo.getPageSize(),pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }

    @Override
    public void deleteFileShareBatch(String[] shareIdArray, String userId) {
        Integer count = this.fileShareMapper.deleteFileShareBatch(shareIdArray, userId);
        if (count != shareIdArray.length) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    //外部获取分享的信息
    @Override
    public ShareInfoVo getShareInfoVo(HttpServletRequest request,String shareId) {
        ShareInfoVo vo = fileShareMapper.getShareInfoByShareId(shareId);
        //判断分享链接是否失效
        if(vo==null || new Date().after(vo.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //判断是否是当前用户分享的文件
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        if(userId.equals(vo.getUserId())){
            vo.setCurrentUser(true);//展示“取消分享”按钮
        }else {
            vo.setCurrentUser(false);//展示“保存到我的网盘”按钮
        }
        return vo;
    }

    @Override
    public void checkShareCode(HttpServletResponse response, String shareId, String code) {
        FileShare fileShare=fileShareMapper.selectByShareId(shareId);
        //判断分享链接是否失效
        if(fileShare==null || new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //校验提取码
        if(!fileShare.getCode().equals(code)){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        //更新浏览次数(在sql语句中添加次数，避免并发问题)
        fileShareMapper.updateShowCountByShareId(shareId);
    }

    @Override
    public PageFileInfoVo loadDataList(Integer pageNo, Integer pageSize, String shareId,String filePid) {
        //校验shareId
        if(StringTools.isEmpty(shareId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        //判断分享链接是否失效
        if(fileShare==null || new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        //开始查询
        String fileId = fileShare.getFileId();
        String userId = fileShare.getUserId();
        List<FileInfoVo> fileInfoVolist;
        PageHelper.startPage(pageNo, pageSize);
        if(filePid.equals("0")){//访问根目录时，展示分享的文件或目录
            FileInfo info = fileInfoMapper.selectByUserIdAndFileId(fileId, userId, 0);
            if(info==null){
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            fileInfoVolist=new ArrayList<>();
            fileInfoVolist.add(new FileInfoVo(info));
        }else {//访问分享的目录时,根据filePid展示目录下的文件
            //TODO 校验filePid是分享的目录fileId下的文件
            //找filePid下所有文件
            fileInfoVolist=fileInfoMapper.selectByUserIdAndCategory(null,userId,filePid);
        }
        PageInfo<FileInfoVo> pageInfo = new PageInfo<>(fileInfoVolist);
        return new PageFileInfoVo(pageInfo.getTotal(),pageInfo.getPageSize(),pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }
}
