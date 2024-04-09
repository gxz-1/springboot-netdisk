package com.netdisk.utils;

import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.pojo.FileInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class FileTools {
    //读取文件流并写入response
    public static void readFile(HttpServletResponse response, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (FileInputStream in = new FileInputStream(file);
             //TODO 注意资源一定要及时释放，不然会导致资源被一直占用，在更新头像时报错！
             OutputStream out = response.getOutputStream()) {//try-with-resources语句会自动关闭资源
            byte[] byteData = new byte[1024];
            int len;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            throw new BusinessException("文件读取异常");
        }
    }

     public static void cutVideo(String fileId, String videoPath){
        //创建同名切片目录
        File tsFolder = new File(videoPath.substring(0, videoPath.lastIndexOf(".")));
        if(!tsFolder.exists()){
            tsFolder.mkdirs();
        }
        //调用ffmpeg的命令
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
        //生成ts文件
        String tsPath=tsFolder+"/index.ts";
        String cmd=String.format(CMD_TRANSFER_2TS,videoPath,tsPath);
        ProcessUtils.executeCommand(cmd,false);
        //生成索引文件.m3u8和切片.ts
        cmd=String.format(CMD_CUT_TS,tsPath,tsFolder.getPath()+"/index.m3u8",tsFolder.getPath(),fileId);
        ProcessUtils.executeCommand(cmd,false);
        //删除index.ts
        new File(tsPath).delete();
    }

}
