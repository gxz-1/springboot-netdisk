package com.netdisk.advice;

import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.enums.VerifyRegexEnum;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.JwtHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Aop实现参数校验
@Component//放入ioc容器
@Aspect//设置为切面
public class ValidationAspect {

    @Autowired
    private JwtHelper jwtHelper;

    @Before("execution(* com.netdisk.controller.AccountController.*(..))")//对controller包中AccountController类的所有参数
    private void ValidateParam(JoinPoint point){
        MethodSignature methodSignature = (MethodSignature) point.getSignature();// 获取方法签名
        String methodName = methodSignature.getName();// 获取被拦截的方法名
        Class<?>[] parameterTypes = methodSignature.getParameterTypes();// 获取被拦截的方法的参数类型
        try {
            Method m = point.getTarget().getClass().getMethod(methodName, parameterTypes);
            Parameter[] parameters = m.getParameters();
            Object[] arguments = point.getArgs();
            for (int i = 0; i < parameters.length; i++) {
                String typeName = parameters[i].getParameterizedType().getTypeName();//参数类型名
                String paraName = parameters[i].getName();//参数名
                Object value = arguments[i];//参数值
                //对参数名为"email"的参数进行校验
                if(paraName.equals("email")){
                    regexCheck(VerifyRegexEnum.EMAIL.getRegex(),value);
                } else if (paraName.equals("password")) {
                    regexCheck(VerifyRegexEnum.PASSWORD.getRegex(), value);
                }
                //对String Integer Long这三个类型做校验
                else if(typeName.equals("java.lang.String") || typeName.equals("java.lang.Integer") || typeName.equals("java.lang.Long")){
                    regexCheck(VerifyRegexEnum.COMMON.getRegex(),value);
                }
                //还可以自定义其他校验,待实现...
            }
        } catch (BusinessException e){//捕捉到自定义的BusinessException抛出参数校验失败
            throw e;
        } catch (Exception e) {//其他错误抛出服务器异常
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    //校验登录
    @Before("execution(* com.netdisk.controller.FileInfoController.*(..))" +
            "||execution(* com.netdisk.controller.RecycleController.*(..))"+
            "||execution(* com.netdisk.controller.ShareController.*(..))")//对controller包中AccountController类的所有参数
    private void checkLogin(){
        //拿到request对象
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        //对获取缩略图接口放行，分享时需要调用
        String url = request.getRequestURL().toString();
        if (url.contains("netdisk/api/file/getImage/")){
            return;
        }
        //判断token是否过期
        String token = CookieTools.getCookieValue(request, null, "token", false);
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        if(jwtHelper.isExpiration(token)){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        //判断token与userId是否一致
        if(userId==null || !userId.equals(jwtHelper.getUserId(token)) ){
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    //正则匹配校验,在VerifyRegexEnum中定义了正则校验的规则
    void regexCheck(String Regex,Object value) throws BusinessException{
        if (value == null ) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);//参数不能为空
        }
        Pattern pattern = Pattern.compile(Regex);
        Matcher matcher = pattern.matcher(String.valueOf(value));
        if(!matcher.matches()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

}
