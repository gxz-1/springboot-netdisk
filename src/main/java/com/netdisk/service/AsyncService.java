package com.netdisk.service;

public interface AsyncService {

    //异步方法，将分片的文件合并
    void transferFile(String fileId, String userId);
}
