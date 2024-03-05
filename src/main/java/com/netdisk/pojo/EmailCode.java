package com.netdisk.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;


@Data
public class EmailCode implements Serializable {


    private String email;

    private String code;

    //被序列化为JSON时，将按照"yyyy-MM-dd HH:mm:ss"格式，并且使用GMT+8时区
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    //Spring提供，用于解析时间字符串，将其转换为Java中的Date对象
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer status;

}
