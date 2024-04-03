package com.netdisk.mappers;

import com.netdisk.pojo.FileInfo;
import com.netdisk.vo.FileInfoVo;

import java.util.List;

public interface FileInfoMapper {
    List<FileInfoVo> selectByUserIdAndCategory(Integer category, String userId);

    Long selectUseSpace(String userId);

    FileInfo selectOneByMD5(String fileMd5);

    FileInfo selectOneByUserIdAndfilePidAndfileName();

    void updateFileInfo(FileInfo dbFile);
}

