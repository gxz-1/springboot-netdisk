package com.netdisk.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {

    public static final String getRandomNumber(Integer len) {
        //使用commons包生成长度为5的只包含数字的随机数
        return RandomStringUtils.random(len, false, true);
    }


}
