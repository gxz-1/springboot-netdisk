package com.netdisk.controller;

import com.netdisk.service.FileShareService;
import com.netdisk.utils.CookieTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ResponseVO;
import com.netdisk.vo.ShareInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/showShare")
public class WebShareController {

    @Autowired
    FileShareService fileShareService;

    //校验提取码界面的分享文件的信息
    @RequestMapping("getShareInfo")
    public ResponseVO getShareInfo(HttpServletRequest request,String shareId){
        ShareInfoVo vo=fileShareService.getShareInfoVo(request,shareId);
        return ResponseVO.getSuccessResponseVO(vo);
    }

    //校验提取码
    @RequestMapping("checkShareCode")
    public ResponseVO checkShareCode(HttpServletResponse response, String shareId, String code){
        fileShareService.checkShareCode(response,shareId,code);
        return ResponseVO.getSuccessResponseVO(null);
    }

    //分享界面的文件信息
    @RequestMapping("getShareLoginInfo")
    public ResponseVO getShareLoginInfo(HttpServletRequest request, String shareId){
        ShareInfoVo vo=fileShareService.getShareInfoVo(request,shareId);
        return ResponseVO.getSuccessResponseVO(vo);
    }


    //获取分享文件列表,传入filePid时表示分享的是文件夹，不传入filePid时表示分享文件
    @RequestMapping(value = "loadFileList",method = RequestMethod.POST)
    public ResponseVO loadDataList(HttpServletRequest request,
                                   @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String shareId, String filePid){
//        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageFileInfoVo pageInfo = fileShareService.loadDataList(pageNo,pageSize,shareId,filePid);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }
}
