package com.netdisk.controller;

import com.netdisk.advice.BusinessException;
import com.netdisk.enums.ResponseCodeEnum;
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

    //分享文件的基本信息（检验提取码前）
    @RequestMapping("getShareInfo")
    public ResponseVO getShareInfo(HttpServletRequest request,String shareId){
        ShareInfoVo vo=fileShareService.getShareInfoVo(request,shareId);
        return ResponseVO.getSuccessResponseVO(vo);
    }

    //分享文件的基本信息（检验提取码后）
    @RequestMapping("getShareLoginInfo")
    public ResponseVO getShareLoginInfo(HttpServletRequest request,String shareId){
        String code = CookieTools.getCookieValue(request, null, "code", false);
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        fileShareService.checkShareCode(shareId,code);
        ShareInfoVo vo=fileShareService.getShareInfoVo(request,shareId);
        return ResponseVO.getSuccessResponseVO(vo);
    }

    //校验提取码
    @RequestMapping("checkShareCode")
    public ResponseVO checkShareCode(HttpServletResponse response, String shareId, String code){
        fileShareService.checkShareCode(shareId,code);
        CookieTools.addCookie(response,"code",code,"/",true,-1);
        return ResponseVO.getSuccessResponseVO(null);
    }


    //获取分享文件列表,filePid="0"展示分享文件，传入filePid时表示分享的是目录，且访问子目录filePid下的文件
    @RequestMapping(value = "loadFileList",method = RequestMethod.POST)
    public ResponseVO loadDataList(@RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String shareId, String filePid){
        PageFileInfoVo pageInfo = fileShareService.loadDataList(pageNo,pageSize,shareId,filePid);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }
}
