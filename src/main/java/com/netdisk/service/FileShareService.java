package com.netdisk.service;

import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ShareInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FileShareService {
    ShareInfoVo getShareInfoVo(HttpServletRequest request, String shareId);

    void checkShareCode(HttpServletResponse response, String shareId, String code);

    PageFileInfoVo loadDataList(Integer pageNo, Integer pageSize, String shareId,String filePid);
}
