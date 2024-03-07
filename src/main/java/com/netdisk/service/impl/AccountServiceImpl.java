package com.netdisk.service.impl;

import com.netdisk.exception.BusinessException;
import com.netdisk.mappers.EmailCodeMapper;
import com.netdisk.mappers.UserInfoMapper;
import com.netdisk.pojo.EmailCode;
import com.netdisk.pojo.UserInfo;
import com.netdisk.service.AccountService;
import com.netdisk.utils.StringTools;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private JavaMailSender mailSender;//spring提供的邮箱操作对象

    @Value("${spring.mail.username}")
    String fromEmail;//在配置中拿到发送的邮箱

    //发送邮箱验证码
    @Override
    @Transactional(rollbackFor = Exception.class)
    //默认只对运行时异常RuntimeException和错误Error回滚
    // 这里指定任何异常都回滚，即额外包含非运行时异常
    public void sendEmailCode(String email, Integer type) {
        //0.如果是注册(type=0)，要求邮箱在用户表中不存在;找回密码(type=1)时则不需要
        if(type==0){
            UserInfo userInfo=userInfoMapper.selectByEmail(email);
            if(userInfo!=null){
                throw new BusinessException("邮箱已经被用户使用");
            }
        }
        //1.生成长度为5的随机数
        String code= StringTools.getRandomNumber(5);
        //2.每次发送前需要重置之前存储的验证码
        emailCodeMapper.disableEmailCode(email);
        //3.存储验证码到EmailCode表中
        EmailCode emailCode=new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);
        //4.向邮箱发送code
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(fromEmail);
            helper.setTo(email);
            if(type==0){
                helper.setSubject("netdisk网盘注册验证码");
            }else if(type==1){
                helper.setSubject("netdisk网盘找回密码验证码");
            }
            helper.setText(String.format("你好，验证码是：%s，5分钟有效。", code));
            helper.setSentDate(new Date());
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException("邮件发送失败");
        }
    }
}
