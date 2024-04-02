package com.netdisk.controller;

import com.github.pagehelper.PageInfo;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.service.FileInfoService;
import com.netdisk.utils.CookieTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/file")
public class FileInfoController {

    @Autowired
    FileInfoService fileInfoService;

    @RequestMapping("loadDataList")
    public ResponseVO loadDataList(HttpServletRequest request, HttpServletResponse response,
                                  Integer pageNo,Integer pageSize,String category){
        String userId = CookieTools.getCookieValue(request, response, "userId", false);
        PageInfo<FileInfoVo> pageInfo = fileInfoService.selectPageFileInfo(pageNo,pageSize,category, userId);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }
}
