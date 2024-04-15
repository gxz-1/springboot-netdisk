package com.netdisk.controller;

import com.netdisk.pojo.FileShare;
import com.netdisk.service.FileShareService;
import com.netdisk.utils.CookieTools;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ResponseVO;
import com.netdisk.vo.ShareInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/share")
public class ShareController {

    @Autowired
    private FileShareService fileShareService;

    //创建分享链接
    @RequestMapping("shareFile")
    public ResponseVO shareFile(HttpServletRequest request,String fileId,Integer validType, String code) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        FileShare share=fileShareService.saveShare(userId,fileId,validType,code);
        return ResponseVO.getSuccessResponseVO(share);
    }

    //展示分享列表
    @RequestMapping("loadShareList")
    public ResponseVO loadShareList(HttpServletRequest request,
                                    @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageFileInfoVo resultVO = fileShareService.findListByPage(pageNo,pageSize,userId);
        return ResponseVO.getSuccessResponseVO(resultVO);
    }

    //取消分享
    @RequestMapping("cancelShare")
    public ResponseVO cancelShare(HttpServletRequest request, String shareIds) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileShareService.deleteFileShareBatch(shareIds.split(","), userId);
        return ResponseVO.getSuccessResponseVO(null);
    }
}
