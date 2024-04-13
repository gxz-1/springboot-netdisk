package com.netdisk.service;

import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileInfoService {
    void updatePassword(String userId, String password);

    PageFileInfoVo selectPageFileInfo(Integer pageNo, Integer pageSize,
                                      String category, String userId, String filePid);

    Map uploadFile(HttpServletRequest request, HttpServletResponse response,
                          String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks);

    void getVideoInfo(HttpServletResponse response,String fileId,String userId);

    void getFileInfo(HttpServletResponse response, String fileId, String userId);

    FileInfoVo createFolder(String filePid, String userId, String fileName);

    List<FileInfoVo> getFolderInfo(String path, String userId);

    FileInfoVo fileRename(String fileId, String userId, String fileName);

    List<FileInfoVo> loadAllFolder(String userId, String filePid, String currentFileIds);

    void changeFileFolder(String fileIds, String userId, String filePid);

    String createDownloadToken(String fileId);

    void downloadFile(HttpServletRequest request,HttpServletResponse response,String downloadToken, String userId);

    void deleteFile(HttpServletRequest request,HttpServletResponse response,String userId, String fileIds);


}
