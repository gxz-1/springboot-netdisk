package com.netdisk.enums;


public enum ResponseCodeEnum {
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_500(500, "服务器返回错误，请联系管理员"),
    CODE_901(901, "登录超时，请重新登录"),
    CODE_902(902, "分享连接不存在，或者已失效"),
    CODE_903(903, "分享验证失效，请重新验证"),
    CODE_904(904, "网盘空间不足，请扩容"),
    CODE_905(905, "同名文件已经存在"),

    CODE_801(801,"验证码已超时，请刷新"),
    CODE_802(802,"图片验证码不正确"),
    CODE_803(803,"获取cookie数据失败"),
    CODE_804(804,"邮箱已经被用户使用"),
    CODE_805(805,"邮件发送失败"),
    CODE_806(806,"邮箱验证失败"),
    CODE_807(807,"邮箱验证码已失效，请重试"),
    CODE_808(808,"昵称已经存在"),
    CODE_809(809,"用户名或密码错误"),
    CODE_810(810,"账号已停用"),
    CODE_811(811,"上传头像失败");
    private Integer code;
    private String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
