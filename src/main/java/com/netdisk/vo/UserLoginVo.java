package com.netdisk.vo;

import com.netdisk.pojo.UserInfo;
import lombok.Data;

//用户登录返回的vo
@Data
public class UserLoginVo {
    private String nickName;
    private String userId;
    private Long avatar;
    private boolean admin;
    private String useSpace;
    private String totalSpace;

    public UserLoginVo(UserInfo userInfo){
        this.nickName=userInfo.getNickName();
        this.userId=userInfo.getUserId();
        this.admin=false;//TODO: 暂时不处理管理员相关逻辑
        this.avatar=null;//TODO: 后续设置
        this.useSpace= String.valueOf(userInfo.getUseSpace());
        this.totalSpace= String.valueOf(userInfo.getTotalSpace());
    }
}
