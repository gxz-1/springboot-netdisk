package com.netdisk.controller;

import com.netdisk.utils.CreateImageCode;
import com.netdisk.exception.BusinessException;
import com.netdisk.service.AccountService;
import com.netdisk.utils.ResponseVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api")
public class AccountController {

    @Autowired
    AccountService accountService;

    //获取验证码图片
    @RequestMapping("checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session,Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            //type默认为0.生成登录注册的验证码
            session.setAttribute("check_code_key", code);
            System.out.println(session.getId());
            System.out.println(session.getAttribute("check_code_key"));
        } else {
            //type为1时，生成邮箱验证码
            session.setAttribute("check_code_key_email", code);
            System.out.println(session.getId());
            System.out.println(session.getAttribute("check_code_key_email"));
        }
        vCode.write(response.getOutputStream());
    }

    //获取邮箱验证码
    @PostMapping("sendEmailCode")
    public ResponseVO sendEmailCode(HttpSession session,String email,String checkCode,Integer type){
        try {
            //校验验证码
            if(!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code_key_email"))){
                System.out.println(session.getId());
                System.out.println(session.getAttribute("check_code_key_email"));
                System.out.println(session.getAttribute("check_code_key"));
                throw new BusinessException("图片验证码不正确");
            }
            //生成邮箱验证码
            accountService.sendEmailCode(email,type);
            return ResponseVO.getSuccessResponseVO(null);
        }finally {
            session.removeAttribute("check_code_key_email");//重置验证码
        }
    }

}
