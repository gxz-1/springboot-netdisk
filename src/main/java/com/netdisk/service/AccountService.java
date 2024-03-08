package com.netdisk.service;

import com.netdisk.vo.UserLoginVo;

public interface AccountService {
    void sendEmailCode(String email, Integer type);

    void checkEmailCode(String email, String code);

    void register(String email, String nickName, String password);

    UserLoginVo login(String email, String password);
}
