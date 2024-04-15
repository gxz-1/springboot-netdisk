package com.netdisk.mappers;

import com.netdisk.pojo.FileInfo;
import com.netdisk.vo.FileInfoVo;

import java.util.Date;
import java.util.List;

public interface FileInfoMapper {
    List<FileInfoVo> selectByUserIdAndCategory(Integer category, String userId,String filePid);

    List<FileInfo> selectListByUserIdAndFilePid(String userId, String filePid);

    Long selectUseSpace(String userId);

    FileInfo selectOneByMD5(String fileMd5);

    FileInfo selectSameNameFile(String userId,String filePid,String fileName,Integer folderType);

    void updateFileInfo(FileInfo info);

    void insertFileInfo(FileInfo info);

    FileInfo selectByUserIdAndFileId(String fileId, String userId,Integer folderType);

    List<FileInfoVo> selectFoldersByFilePid(String filePid, String userId);

    void updateDelFlagByFileIdAndUserId(String fileId, String userId, Integer delFlag, Date time);

    List<FileInfoVo> selectDelFileList(String userId);

    FileInfo selectDelFileByUserIdAndFileId(String fileId, String userId);
}

