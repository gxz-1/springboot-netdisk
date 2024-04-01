package com.netdisk;

import com.netdisk.advice.BusinessException;
import com.netdisk.utils.StringTools;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Date;


@SpringBootTest
public class SpringTest {

    @Autowired
    private JavaMailSender mailSender;//spring提供的邮箱操作对象

    @Value("${spring.mail.username}")
    String fromEmail;//在配置中拿到发送的邮箱

//    @Test
//    public void testMailSend(){
//        String code= StringTools.getRandomNumber(5);
//        //4.向邮箱发送code
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message);
//        try {
//            helper.setFrom(fromEmail);
//            helper.setTo("582783768@qq.com");
//            helper.setSubject("netdisk网盘注册验证码");
//            helper.setText(String.format("你好，验证码是：%s，5分钟有效。", code));
//            helper.setSentDate(new Date());
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new BusinessException("邮件发送失败");
//        }
//    }
}
