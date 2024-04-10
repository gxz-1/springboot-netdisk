package com.netdisk.service;

import com.github.pagehelper.PageInfo;
import com.netdisk.pojo.FileInfo;
import com.netdisk.vo.FileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileInfoService {
    PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize,
                                            String category, String userId,String filePid);

    Map uploadFile(HttpServletRequest request, HttpServletResponse response,
                          String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks);

    //将分片的文件合并
    void transferFile(String fileId,String userId);


    void getVideoInfo(HttpServletResponse response,String fileId,String userId);

    void getFileInfo(HttpServletResponse response, String fileId, String userId);

    FileInfoVo createFolder(String filePid, String userId, String fileName);

    List<FileInfoVo> getFolderInfo(String path, String userId);

    FileInfoVo fileRename(String fileId, String userId, String fileName);

    List<FileInfoVo> loadAllFolder(String userId, String filePid, String currentFileIds);

    void changeFileFolder(String fileIds, String userId, String filePid);

    void downloadFile(HttpServletRequest request,HttpServletResponse response,String code, String userId);

    void deleteFile(String userId, String fileIds);
}
