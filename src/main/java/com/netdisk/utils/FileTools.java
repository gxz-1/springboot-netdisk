package com.netdisk.utils;

import com.netdisk.advice.BusinessException;
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

}
