package com.netdisk.service;

import com.netdisk.vo.PageFileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RecycleService {
    PageFileInfoVo selectPageFileInfo(Integer pageNo, Integer pageSize, String userId);

    void recoverFile(HttpServletRequest request, HttpServletResponse response,String fileIds, String userId);

    void delFile(String fileIds, String userId);

}
