package com.netdisk.service.impl;

import com.netdisk.mappers.ScheduleMapper;
import com.netdisk.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    ScheduleMapper scheduleMapper;

    @Value("${my.outFileFolder}")
    String outFileFolder;

    @Override
    @Scheduled(cron = "0 0 4 * * *")//每天凌晨4点执行
    public void deleteTrash(){
        System.out.println("-------------------------------执行回收站垃圾清除--------------------------");
        //将回收站中存在超过10天的文件删除（delFlag由1更新为0）
        scheduleMapper.updateByRecoveryTime();
    }

    @Override
    @Scheduled(cron = "0 0 0 */7 * ?")  // 每7天执行一次，午夜执行
    public void deleteDatabase(){
        System.out.println("-------------------------------执行数据库清理--------------------------");
        //删除status=1已使用的邮箱验证码
        scheduleMapper.deleteEmailCode();
        //删除delFlag=0的文件夹（删除的文件保留，用于MD5秒传）
        scheduleMapper.deleteFile();
        //删除expireTime大于当前时间的分享链接
        scheduleMapper.deleteShare();
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")  //每天凌晨3点执行
    public void deleteTempFile(){
        System.out.println("-------------------------------执行临时文件清理--------------------------");
        //清理temp目录下最后修改时间超过1天的文件夹
        Path tempDir = Paths.get(outFileFolder+"/temp");
        File[] files = tempDir.toFile().listFiles();
        if (files != null) {
            Arrays.stream(files).forEach(file -> {
                try {
                    // 检查文件的最后修改时间是否超过1天
                    if (Files.getLastModifiedTime(file.toPath()).toMillis() < System.currentTimeMillis() - 86400000) {
                        Files.delete(file.toPath());
                        System.out.println("临时文件已清理: " + file.getName());
                    }
                } catch (Exception e) {
                    System.err.println("临时文件清理失败: " + file.getName() + ", " + e.getMessage());
                }
            });
        }
    }

}
