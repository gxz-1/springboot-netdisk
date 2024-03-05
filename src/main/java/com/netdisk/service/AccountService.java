package com.netdisk.service;

public interface AccountService {
    void sendEmailCode(String email, Integer type);
}
