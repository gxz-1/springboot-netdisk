package com.netdisk.service.impl;

import com.netdisk.exception.BusinessException;
import com.netdisk.mappers.EmailCodeMapper;
import com.netdisk.mappers.UserInfoMapper;
import com.netdisk.pojo.EmailCode;
import com.netdisk.pojo.UserInfo;
import com.netdisk.service.AccountService;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.UserLoginVo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
//默认只对运行时异常RuntimeException和错误Error回滚
// 这里指定任何异常都回滚，即额外包含非运行时异常(如自定义的BusinessException)
public class AccountServiceImpl implements AccountService {

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private JavaMailSender mailSender;//spring提供的邮箱操作对象

    @Value("${my.invalidEmailTime}")
    private Integer invalidEmailTime;//邮箱验证码的过期时间,单位:分钟

    @Value("${my.TotalSpace}")
    private Long TotalSpace;//用户的网盘空间,单位:MB

    @Value("${spring.mail.username}")
    private String fromEmail;//在配置中拿到发送的邮箱

    //发送邮箱验证码
    @Override
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
            helper.setText(String.format("你好，验证码是：%s，%d分钟有效。", code,invalidEmailTime));
            helper.setSentDate(new Date());
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException("邮件发送失败");
        }
    }

    //校验邮箱验证码
    @Override
    public void checkEmailCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email,code);
        if(emailCode==null){
            throw new BusinessException("邮箱验证失败");
        }
        long diffMinutes = (new Date().getTime() - emailCode.getCreateTime().getTime()) / (60 * 1000);
        if(emailCode.getStatus()==1 || diffMinutes > 5){
            throw new BusinessException("邮箱验证已失效，请重试");
        }
    }

    //注册用户
    @Override
    public void register(String email, String nickName, String password) {
        //要求昵称全局唯一
        if(userInfoMapper.selectBynickName(nickName)!=null){
            throw new BusinessException("昵称已经存在");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(StringTools.getRandomNumber(10));
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(DigestUtils.md5Hex(password));
        userInfo.setJoinTime(new Date());
        userInfo.setUseSpace(0l);
        userInfo.setTotalSpace(TotalSpace*1024*1024);
        userInfoMapper.insert(userInfo);
    }

    //用户登录
    @Override
    public UserLoginVo login(String email, String password) {
        //校验密码和用户状态status
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if(userInfo==null || !userInfo.getPassword().equals(password)){//前端已将密码加密
            throw new BusinessException("用户名或密码错误");
        }
        if(userInfo.getStatus()==1){
            throw new BusinessException("账号已停用");
        }
        //更新登录时间
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.updateUserInfo(userInfo);
        //设置返回vo
        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setNickName(userInfo.getNickName());
        userLoginVo.setUserId(userInfo.getUserId());
        userLoginVo.setAdmin(false);//TODO: 暂时不处理管理员相关逻辑
        userLoginVo.setAvatar(null);//TODO: 后续设置
        return userLoginVo;
    }
}
