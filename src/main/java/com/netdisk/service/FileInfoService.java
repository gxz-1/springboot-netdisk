package com.netdisk.service;

import com.github.pagehelper.PageInfo;
import com.netdisk.vo.FileInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileInfoService {
    PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize, String category, String userId);

    Map uploadFile(HttpServletRequest request, HttpServletResponse response,
                          String fileId, MultipartFile file, String fileName, String filePid,
                          String fileMd5, Integer chunkIndex, Integer chunks);

    //将分片的文件合并
    void transferFile(String fileId,String userId);


    void getVideoInfo(HttpServletResponse response,String fileId,String userId);

    void getFileInfo(HttpServletResponse response, String fileId, String userId);
}
