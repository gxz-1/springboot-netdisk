package com.netdisk.controller;

import com.netdisk.service.RecycleService;
import com.netdisk.utils.CookieTools;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/recycle")
public class RecycleController {

    @Autowired
    private RecycleService recycleService;

    //展示回收站文件列表
    @RequestMapping("loadRecycleList")
    public ResponseVO loadRecycleList(HttpServletRequest request,
                                      @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageFileInfoVo pageInfo = recycleService.selectPageFileInfo(pageNo,pageSize,userId);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }

    //恢复文件到根目录
    @RequestMapping("recoverFile")
    public ResponseVO recoverFile(HttpServletRequest request, HttpServletResponse response, String fileIds){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        recycleService.recoverFile(request,response,fileIds,userId);
        return ResponseVO.getSuccessResponseVO(null);
    }

    //彻底删除
    @RequestMapping("delFile")
    public ResponseVO delFile(HttpServletRequest request,String fileIds){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        recycleService.delFile(fileIds,userId);
        return ResponseVO.getSuccessResponseVO(null);
    }
}
