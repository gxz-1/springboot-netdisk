package com.netdisk.controller;

import com.netdisk.exception.BusinessException;
import com.netdisk.service.AccountService;
import com.netdisk.utils.CreateImageCode;
import com.netdisk.vo.ResponseVO;
import com.netdisk.vo.UserLoginVo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api")
public class AccountController {

    @Autowired
    private AccountService accountService;

    //获取验证码图片
    @RequestMapping("checkCode")
    public void checkCode(HttpServletResponse response, Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            //type默认为0.生成登录注册的验证码并存储到Cookie
            Cookie cookie = new Cookie("check_code_key", code);
            cookie.setPath("/"); // 设置Cookie适用的路径
            cookie.setHttpOnly(true); // 增加安全性，防止JS直接访问
            cookie.setMaxAge(5 * 60); // 设置Cookie的存活时间，例如5分钟
            response.addCookie(cookie);
        } else {
            //type为1时，生成邮箱验证码
            Cookie cookie = new Cookie("check_code_key_email", code);
            cookie.setPath("/"); // 设置Cookie适用的路径
            cookie.setHttpOnly(true); // 增加安全性，防止JS直接访问
            cookie.setMaxAge(5 * 60); // 设置Cookie的存活时间，例如5分钟
            response.addCookie(cookie);
        }
        vCode.write(response.getOutputStream());
    }

    //获取邮箱验证码
    @RequestMapping(value = "sendEmailCode",method = RequestMethod.POST)
    public ResponseVO sendEmailCode(HttpServletRequest request, HttpServletResponse response,
                                    String email, String checkCode, Integer type){
        // 从请求中获取Cookie中的验证码
        String codeFromCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("check_code_key_email".equals(cookie.getName())) {
                    codeFromCookie = cookie.getValue();
                    //获取后立即过期
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        //cookie超时失效
        if(codeFromCookie==null){
            throw new BusinessException("验证码已超时，请刷新");
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(codeFromCookie)){//忽略大小写
            throw new BusinessException("图片验证码不正确");
        }
        //发送邮箱验证码
        accountService.sendEmailCode(email,type);
        return ResponseVO.getSuccessResponseVO(null);
    }

    //用户注册
    @RequestMapping(value = "register",method = RequestMethod.POST)
    public ResponseVO register(HttpServletRequest request, HttpServletResponse response,
                               String email,String nickName,String password,String checkCode,String emailCode){
        // 从请求中获取Cookie中的验证码
        String codeFromCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("check_code_key".equals(cookie.getName())) {
                    codeFromCookie = cookie.getValue();
                    //获取后立即过期
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        //cookie超时失效
        if(codeFromCookie==null){
            throw new BusinessException("验证码已超时，请刷新");
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(codeFromCookie)){//忽略大小写
            throw new BusinessException("图片验证码不正确");
        }
        accountService.checkEmailCode(email,emailCode);
        accountService.register(email,nickName,password);

        return ResponseVO.getSuccessResponseVO(null);
    }

    //用户登录
    @RequestMapping(value = "login",method = RequestMethod.POST)
    public ResponseVO login(HttpServletRequest request, HttpServletResponse response,
                               String email,String password,String checkCode){
        // 从请求中获取Cookie中的验证码
        String codeFromCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("check_code_key".equals(cookie.getName())) {
                    codeFromCookie = cookie.getValue();
                    //获取后立即过期
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        //cookie超时失效
        if(codeFromCookie==null){
            throw new BusinessException("验证码已超时，请刷新");
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(codeFromCookie)){//忽略大小写
            throw new BusinessException("图片验证码不正确");
        }
        //登录
        UserLoginVo userLoginVo = accountService.login(email,password);
        //添加到cookie并返回
        response.addCookie(new Cookie("nickName", userLoginVo.getNickName()));
        response.addCookie(new Cookie("userId", userLoginVo.getUserId()));
        return ResponseVO.getSuccessResponseVO(userLoginVo);
    }

}
