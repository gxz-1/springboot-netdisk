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
        //实现多线程下载
        //1.将下面的下载代码改为下载某个分片，并包装为一个线程类，继承Runnable
        //2.通过ExecutorService创建线程池管理每个线程
        //3.合并下载的分片为目标下载文件
        try (FileInputStream in = new FileInputStream(file);
             //TODO 注意资源一定要及时释放，不然会导致资源被一直占用，在更新头像时报错！
             OutputStream out = response.getOutputStream()) {//try-with-resources语句会自动关闭资源
            byte[] byteData = new byte[1024];
            int len;
            while ((len = in.read(byteData)) != -1) {
//                //如果需要实现限速下载，1.在这里统计已经下载的字节总数totalBytesRead
//                long totalBytesRead += len;
//                //2.根据totalBytesRead计算当前速率并与限制的速率比较
//                if (totalBytesRead/Time > DOWNLOAD_SPEED_LIMIT) {
//                    //3.暂停一段时间以控制速率
//                    long idealTime = (totalBytesRead * 1000) / DOWNLOAD_SPEED_LIMIT;
//                    long sleepTime = idealTime - Time;
//                    Thread.sleep(sleepTime);
//                }
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
        //调用ffmpeg的命令(从 ffmpeg 4.2 开始，-vbsf 参数已经被弃用。应该使用 -bsf:v 替代)
         final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s -c copy -bsf:v %s %s";
         final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
         final String CMD_PROBE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s";
         // 检测视频编码
         String probeCmd = String.format(CMD_PROBE, videoPath);
         String videoCodec = ProcessUtils.executeCommand(probeCmd, true);
         // 确定合适的过滤器
         String bsf;
         if ("h264".equals(videoCodec)) {
             bsf = "h264_mp4toannexb";
         } else if ("hevc".equals(videoCodec)) {
             bsf = "hevc_mp4toannexb";
         } else {
             bsf = "null";  // 无需使用过滤器
         }
         // 生成 ts 文件
         String tsPath = tsFolder + "/index.ts";
         String cmd = String.format(CMD_TRANSFER_2TS, videoPath, bsf, tsPath);
         ProcessUtils.executeCommand(cmd, false);
         // 生成索引文件 .m3u8 和切片 .ts
         cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/index.m3u8", tsFolder.getPath(), fileId);
         ProcessUtils.executeCommand(cmd, false);
         // 删除 index.ts
         new File(tsPath).delete();
    }

}
