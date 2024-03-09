package com.netdisk.vo;

import com.netdisk.enums.ResponseCodeEnum;
import lombok.Data;

@Data
public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    public static  <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus("success");
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    public static  <T> ResponseVO getBusinessExceptionVO(ResponseCodeEnum codeEnum) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus("error");
        responseVO.setCode(codeEnum.getCode());
        responseVO.setInfo(codeEnum.getMsg());
        return responseVO;
    }
}
