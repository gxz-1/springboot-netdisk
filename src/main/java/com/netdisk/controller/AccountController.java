package com.netdisk.controller;

import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.advice.BusinessException;
import com.netdisk.mappers.UserInfoMapper;
import com.netdisk.pojo.UserInfo;
import com.netdisk.service.AccountService;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.CreateImageCode;
import com.netdisk.utils.FileTools;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.ResponseVO;
import com.netdisk.vo.UserLoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserInfoMapper userInfoMapper;

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
            CookieTools.addCookie(response,"check_code_key",code,"/",true,5);
        } else {
            //type为1时，生成邮箱验证码
            CookieTools.addCookie(response,"check_code_key_email",code,"/",true,5);
        }
        vCode.write(response.getOutputStream());
    }

    //获取邮箱验证码
    @RequestMapping(value = "sendEmailCode",method = RequestMethod.POST)
    public ResponseVO sendEmailCode(HttpServletRequest request, HttpServletResponse response,
                                    String email, String checkCode, Integer type){
        // 从请求中获取Cookie中的验证码
        String code = CookieTools.getCookieValue(request,response,"check_code_key_email",true);
        //cookie超时失效
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_801);
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(code)){//忽略大小写
            throw new BusinessException(ResponseCodeEnum.CODE_802);
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
        String code = CookieTools.getCookieValue(request,response,"check_code_key",true);
        //cookie超时失效
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_801);
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(code)){//忽略大小写
            throw new BusinessException(ResponseCodeEnum.CODE_802);
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
        String code = CookieTools.getCookieValue(request,response,"check_code_key",true);
        //cookie超时失效
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_801);
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(code)){//忽略大小写
            throw new BusinessException(ResponseCodeEnum.CODE_802);
        }
        //登录
        UserLoginVo userLoginVo = accountService.login(email,password);
        //添加到cookie并返回
        CookieTools.addCookie(response,"nickName",userLoginVo.getNickName(),"/",true,-1);
        CookieTools.addCookie(response,"userId",userLoginVo.getUserId(),"/",true,-1);
        CookieTools.addCookie(response,"totalSpace", String.valueOf(userLoginVo.getTotalSpace()),"/",true,-1);
        CookieTools.addCookie(response,"useSpace", String.valueOf(userLoginVo.getUseSpace()),"/",true,-1);
        return ResponseVO.getSuccessResponseVO(userLoginVo);
    }

    //重置密码
    @RequestMapping(value = "resetPwd",method = RequestMethod.POST)
    public ResponseVO resetPwd(HttpServletRequest request, HttpServletResponse response,
                               String email,String password,String checkCode,String emailCode){
        // 从请求中获取Cookie中的验证码
        String code = CookieTools.getCookieValue(request,response,"check_code_key",true);
        //cookie超时失效
        if(code==null){
            throw new BusinessException(ResponseCodeEnum.CODE_801);
        }
        //校验验证码
        if(!checkCode.equalsIgnoreCase(code)){//忽略大小写
            throw new BusinessException(ResponseCodeEnum.CODE_802);
        }
        accountService.checkEmailCode(email,emailCode);
        accountService.resetPwd(email,password);
        return ResponseVO.getSuccessResponseVO(null);
    }

    @Value("${my.outFileFolder}")
    private String outFileFolder;

    //获取用户头像
    @RequestMapping(value = "getAvatar/{userId}",method = RequestMethod.GET)
    public void getAvatar(HttpServletResponse response,@PathVariable String userId){
        String avatarFolder=outFileFolder+"/avatar/";//头像文件存储目录
        File folder=new File(avatarFolder);
        if(!folder.exists()){
            // 使用mkdirs而不是mkdir以确保创建所有不存在的父目录
            folder.mkdirs();
        }
        String avatarFile=avatarFolder+userId+".jpg";
        if(!new File(avatarFile).exists()){
            //头像不存在则使用默认头像
            avatarFile=avatarFolder+"default.jpg";//TODO 需要将默认头像放置在这个目录下
        }
        response.setContentType("image/jpg");
        FileTools.readFile(response,avatarFile);//读取文件流并写入response
    }

    //从cookie中获取网盘总空间和使用空间
    @RequestMapping("getUseSpace")
    public ResponseVO getUseSpace(HttpServletRequest request){
        Map res=new HashMap();
        String totalSpace = CookieTools.getCookieValue(request, null, "totalSpace", false);
        String useSpace = CookieTools.getCookieValue(request, null, "useSpace", false);
        res.put("totalSpace",totalSpace);
        res.put("useSpace",useSpace);
        return ResponseVO.getSuccessResponseVO(res);
    }

    //退出登录，清空cookie
    @RequestMapping("logout")
    public ResponseVO logout(HttpServletRequest request,HttpServletResponse response) {
        CookieTools.clearCookie(request,response);
        return ResponseVO.getSuccessResponseVO(null);
    }

    @RequestMapping(value = "updateUserAvatar",method = RequestMethod.POST)
    public ResponseVO updateUserAvatar(HttpServletRequest request, MultipartFile avatar) {
        String avatarFolder=outFileFolder+"/avatar/";//头像文件存储目录
        File folder=new File(avatarFolder);
        if(!folder.exists()){
            // 使用mkdirs而不是mkdir以确保创建所有不存在的父目录
            folder.mkdirs();
        }
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        //没获取到userId
        if (userId==null){
            throw new BusinessException(ResponseCodeEnum.CODE_803);
        }
        File avatarFile = new File(avatarFolder + userId + ".jpg");
        try {
            if(avatarFile.exists()){//存在则先删除
                avatarFile.delete();
            }
            avatar.transferTo(avatarFile);//存储头像
        } catch (Exception e) {
            throw new BusinessException(ResponseCodeEnum.CODE_811);
        }
        //TODO 上传头像后覆盖qq头像  暂时没用到
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserId(userId);
//        userInfo.setQqAvatar("");
//        userInfoMapper.updateUserInfo(userInfo);
        return ResponseVO.getSuccessResponseVO(null);
    }

    //更新密码 TODO 这个接口感觉很危险，可以直接修改密码,暂时不开启
//    @RequestMapping(value = "updatePassword",method = RequestMethod.POST)
    public ResponseVO updatePassword(HttpServletRequest request,String password) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        //没获取到userId
        if (userId==null){
            throw new BusinessException(ResponseCodeEnum.CODE_803);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfoMapper.updateUserInfo(userInfo);
        return ResponseVO.getSuccessResponseVO(null);
    }




}
