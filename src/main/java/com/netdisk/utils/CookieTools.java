package com.netdisk.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieTools {
    public static String getCookieValue(HttpServletRequest request, HttpServletResponse response,
                                        String cookieName,Boolean isValid){
        String cookieValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    cookieValue = cookie.getValue();
                    if(isValid){
                        //获取后立即过期
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                    break;
                }
            }
        }
        return cookieValue;
    }

    public static void addCookie(HttpServletResponse response,
                                 String cookieName,String cookieValue,
                                 String path,Boolean httponly,Integer MaxMin){
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(path); // 设置Cookie适用的路径
        cookie.setHttpOnly(httponly); // 增加安全性，防止JS直接访问
        cookie.setMaxAge(MaxMin * 60); // 设置Cookie的存活时间，MaxMin分钟
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                //获取后立即过期
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
