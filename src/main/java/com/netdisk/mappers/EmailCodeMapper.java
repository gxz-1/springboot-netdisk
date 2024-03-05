package com.netdisk.mappers;

import com.netdisk.pojo.EmailCode;

public interface EmailCodeMapper {
    void disableEmailCode(String email);

    void insert(EmailCode emailCode);
}
