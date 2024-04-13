package com.netdisk.service;

import com.netdisk.vo.UserLoginVo;
import jakarta.servlet.http.HttpServletResponse;

public interface AccountService {
    void sendEmailCode(String email, Integer type);

    void checkEmailCode(String email, String code);

    void register(String email, String nickName, String password);

    UserLoginVo login(HttpServletResponse response, String email, String password);

    void resetPwd(String email, String password);
}
