package com.netdisk.service;

import com.netdisk.pojo.FileShare;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ShareInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FileShareService {

    FileShare saveShare(String userId, String fileId, Integer validType, String code);

    PageFileInfoVo findListByPage(Integer pageNo, Integer pageSize, String userId);

    void deleteFileShareBatch(String[] shareIdArray, String userId);

    ShareInfoVo getShareInfoVo(HttpServletRequest request, String shareId);

    void checkShareCode(HttpServletResponse response, String shareId, String code);

    PageFileInfoVo loadDataList(Integer pageNo, Integer pageSize, String shareId,String filePid);


}
