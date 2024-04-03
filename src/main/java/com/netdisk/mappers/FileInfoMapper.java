package com.netdisk.mappers;

import com.netdisk.pojo.FileInfo;
import com.netdisk.vo.FileInfoVo;

import java.util.List;

public interface FileInfoMapper {
    List<FileInfoVo> selectByUserIdAndCategory(Integer category, String userId);

    Long selectUseSpace(String userId);

    FileInfo selectOneByMD5(String fileMd5);

    FileInfo selectSameNameFile(String userId,String filePid,String fileName);

    void updateFileInfo(FileInfo dbFile);
}

