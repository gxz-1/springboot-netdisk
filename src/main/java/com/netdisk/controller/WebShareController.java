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
import org.springframework.web.bind.annotation.*;

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
    public ResponseVO loadDataList(HttpServletRequest request,
            @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String shareId, String filePid){
        String code = CookieTools.getCookieValue(request, null, "code", false);
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        PageFileInfoVo pageInfo = fileShareService.loadDataList(pageNo,pageSize,shareId,filePid);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }

    //保存到我的网盘
    @RequestMapping("saveShare")
    public ResponseVO saveShare(HttpServletRequest request,
                                String shareId,String shareFileIds,String myFolderId){
        //当前用户必须登录，才能拿到userId
        String myUserId = CookieTools.getCookieValue(request, null, "userId", false);
        if(myUserId==null){
            throw  new BusinessException(ResponseCodeEnum.CODE_600);
        }
        fileShareService.saveShare(shareId,myUserId,shareFileIds,myFolderId);
        return ResponseVO.getSuccessResponseVO(null);
    }

    @RequestMapping(value = "getFolderInfo",method = RequestMethod.POST)
    public ResponseVO getFolderInfo(HttpServletRequest request,String shareId,String path){
        String code = CookieTools.getCookieValue(request, null, "code", false);
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        List<FileInfoVo> res = fileShareService.getFolderInfo(shareId,path);
        return ResponseVO.getSuccessResponseVO(res);
    }

    //读取文件
    @RequestMapping("getFile/{shareId}/{fileId}")
    public void getFileInfo(HttpServletResponse response,
                            @PathVariable String shareId, @PathVariable String fileId){
        fileShareService.getFileInfo(response,shareId,fileId);
    }

    //读取视频文件
    @RequestMapping("ts/getVideoInfo/{shareId}/{fileId}")
    public void getVideoInfo(HttpServletResponse response,
                             @PathVariable String shareId, @PathVariable String fileId){
        fileShareService.getVideoInfo(response,shareId,fileId);
    }

    @RequestMapping("createDownloadUrl/{shareId}/{fileId}")
    public ResponseVO createDownloadUrl(@PathVariable String shareId, @PathVariable String fileId){
        String downloadToken = fileShareService.createDownloadToken(shareId,fileId);
        return ResponseVO.getSuccessResponseVO(downloadToken);
    }

    @RequestMapping("download/{downloadToken}")
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable String downloadToken){
        fileShareService.downloadFile(request,response,downloadToken);
    }
}
