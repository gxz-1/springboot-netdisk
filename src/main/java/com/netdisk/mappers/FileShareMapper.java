package com.netdisk.mappers;

import com.netdisk.pojo.FileShare;
import com.netdisk.vo.FileShareVo;
import com.netdisk.vo.ShareInfoVo;

import java.util.List;

public interface FileShareMapper {
    List<FileShareVo> selectPageByUserId(String userId);

    ShareInfoVo getShareInfoByShareId(String shareId);

    FileShare selectByShareId(String shareId);

    void updateShowCountByShareId(String shareId);

    void insertFileShare(FileShare share);

    Integer deleteFileShareBatch(String[] shareIdArray, String userId);
}
