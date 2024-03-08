package com.netdisk.mappers;

import com.netdisk.pojo.UserInfo;

public interface UserInfoMapper {

    UserInfo selectByEmail(String email);

    UserInfo selectBynickName(String nickName);

    void insert(UserInfo userInfo);

    int updateUserInfo(UserInfo userInfo);
}
