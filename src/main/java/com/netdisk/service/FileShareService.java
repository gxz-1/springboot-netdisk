package com.netdisk.service;

import com.netdisk.pojo.FileShare;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ShareInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FileShareService {

    FileShare createShare(String userId, String fileId, Integer validType, String code);

    PageFileInfoVo findListByPage(Integer pageNo, Integer pageSize, String userId);

    void deleteFileShareBatch(String[] shareIdArray, String userId);

    ShareInfoVo getShareInfoVo(HttpServletRequest request, String shareId);

    void checkShareCode(String shareId, String code);

    PageFileInfoVo loadDataList(Integer pageNo, Integer pageSize, String shareId,String filePid);

    void saveShare(String shareId, String myUserId, String shareFileIds, String myFolderId);

    List<FileInfoVo> getFolderInfo(String shareId, String path);

    void getFileInfo(HttpServletResponse response,String shareId, String fileId);

    void getVideoInfo(HttpServletResponse response, String shareId, String fileId);

    String createDownloadToken(String shareId,String fileId);

    void downloadFile(HttpServletRequest request, HttpServletResponse response, String downloadToken);

}
