package com.netdisk.service;

import com.github.pagehelper.PageInfo;
import com.netdisk.vo.FileInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileInfoService {
    PageInfo<FileInfoVo> selectPageFileInfo(Integer pageNo, Integer pageSize, String category, String userId);

    Map uploadFile(String userId, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);
}
