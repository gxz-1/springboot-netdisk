package com.netdisk.mappers;

import com.netdisk.pojo.FileShare;
import com.netdisk.vo.ShareInfoVo;

public interface FileShareMapper {
    ShareInfoVo getShareInfoByShareId(String shareId);

    FileShare selectByShareId(String shareId);

    void updateShowCountByShareId(String shareId);

}
