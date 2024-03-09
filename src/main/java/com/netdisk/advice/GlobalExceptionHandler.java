package com.netdisk.advice;

import com.netdisk.vo.ResponseVO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//对异常进行全局处理
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    //指定处理的异常
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseVO handleGeneralException(BusinessException e) {
        //向前端返回
        return ResponseVO.getBusinessExceptionVO(e.getCodeEnum());
    }

    // 可以添加更多的异常处理方法来处理特定的异常
}
