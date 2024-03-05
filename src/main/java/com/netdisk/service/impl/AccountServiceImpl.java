package com.netdisk.service.impl;

import com.netdisk.exception.BusinessException;
import com.netdisk.mappers.EmailCodeMapper;
import com.netdisk.mappers.UserInfoMapper;
import com.netdisk.pojo.EmailCode;
import com.netdisk.pojo.UserInfo;
import com.netdisk.service.AccountService;
import com.netdisk.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    EmailCodeMapper emailCodeMapper;

    @Autowired
    UserInfoMapper userInfoMapper;

    //生成邮箱验证码
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String email, Integer type) {
        if(type==0){//type=0时注册 =1时找回密码
            UserInfo userInfo=userInfoMapper.selectByEmail(email);
            if(userInfo!=null){
                throw new BusinessException("邮箱已经被用户使用");
            }
        }
        //生成长度为5的随机数
        String code= StringTools.getRandomNumber(5);
        //每次发送需要重置之前的验证码
        emailCodeMapper.disableEmailCode(email);
        //存储验证码到EmailCode表中
        EmailCode emailCode=new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCodeMapper.insert(emailCode);
    }
}
