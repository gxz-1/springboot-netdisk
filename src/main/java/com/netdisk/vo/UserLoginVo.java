package com.netdisk.vo;

import lombok.Data;

//用户登录返回的vo
@Data
public class UserLoginVo {
    String nickName;
    String userId;
    Long avatar;
    boolean admin;
}
