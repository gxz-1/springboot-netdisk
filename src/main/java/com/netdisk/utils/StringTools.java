package com.netdisk.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {

    public static String getRandomNumber(Integer len) {
        //使用commons包生成长度为len的只包含数字的随机数
        return RandomStringUtils.random(len, false, true);
    }
    
    public static String encodeByMD5(String str){
        return DigestUtils.md5Hex(str);
    }






}
