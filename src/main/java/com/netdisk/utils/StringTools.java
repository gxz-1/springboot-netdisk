package com.netdisk.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {

    public static String getRandomNumber(Integer len) {
        //使用commons包生成长度为len的只包含数字的随机数
        return RandomStringUtils.random(len, false, true);
    }

    public static String getRandomString(Integer len) {
        //使用commons包生成长度为len的包含数字和数字的随机数
        return RandomStringUtils.random(len, true, true);
    }
    
    public static String encodeByMD5(String str){
        return DigestUtils.md5Hex(str);
    }

    //当str为null或空字符或"null"以及"\u0000"(unicode编码中的空字符)判空
    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }




}
