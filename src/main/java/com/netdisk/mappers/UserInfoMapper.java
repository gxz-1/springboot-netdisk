package com.netdisk.mappers;

import com.netdisk.pojo.UserInfo;

public interface UserInfoMapper {

    UserInfo selectByEmail(String email);
}
