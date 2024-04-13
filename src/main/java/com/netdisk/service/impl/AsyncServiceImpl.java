package com.netdisk.service.impl;

import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileTypeEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.mappers.FileInfoMapper;
import com.netdisk.pojo.FileInfo;
import com.netdisk.service.AsyncService;
import com.netdisk.utils.FileTools;
import com.netdisk.utils.ScaleFilter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Service
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Value("${my.outFileFolder}")
    String outFileFolder;

    @Override
    @Async
    public void transferFile(String fileId, String userId) {
        String coverName = null;
        FileTypeEnums typeEnums = null;
        FileInfo fileInfo = fileInfoMapper.selectByUserIdAndFileId(fileId, userId,0);
        if (fileInfo == null || fileInfo.getStatus() != 0) {
            return;//没找到或者文件不在转码中,不处理
        }
        //临时目录
        File tempDirPath = new File(outFileFolder + "/temp/" + userId + fileId);
        File targetFile = new File(fileInfo.getFilePath());
        if (!tempDirPath.exists()) {
            return;//临时文件不存在了
        }
        //开始合并文件
        try {
            RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < tempDirPath.listFiles().length; ++i) {
                int len = -1;
                //创建读块文件的对象
                File chunkFile = new File(tempDirPath.getPath() + File.separator + i);
                RandomAccessFile readFile = new RandomAccessFile(chunkFile, "r");
                while( (len = readFile.read(b)) != -1) {
                    writeFile.write(b, 0, len);
                }
                readFile.close();
            }
            writeFile.close();
            if (tempDirPath.exists()) {//删除临时文件
                FileUtils.deleteDirectory(tempDirPath);
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //视频切割，生成缩略图
        typeEnums=FileTypeEnums.getByType(fileInfo.getFileType());
        if(typeEnums == FileTypeEnums.VIDEO){//视频文件切割
            FileTools.cutVideo(fileId,fileInfo.getFilePath());
            //生成缩略图
            coverName=userId+fileId+".png";
            ScaleFilter.createCover4Video(new File(fileInfo.getFilePath()),150,new File(outFileFolder+"/file/"+coverName));
        }else if(typeEnums == FileTypeEnums.IMAGE){//图片
            coverName=userId+fileId+"_.png";//多加一个_区分缩略图和原图
            Boolean ok = ScaleFilter.createThumbnailWidthFFmpeg(new File(fileInfo.getFilePath()), 150,
                    new File(outFileFolder+"/file/"+coverName), false);
            if(!ok){//压缩失败则复制原图，例如原图太小
                try {
                    FileUtils.copyFile(new File(fileInfo.getFilePath()),new File(outFileFolder+"/file/"+coverName));
                } catch (IOException e) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600);
                }
            }
        }
        //设置文件大小
        FileInfo info = new FileInfo();
        info.setFileSize(targetFile.length());
        info.setFileCover(coverName);
        info.setStatus(2);//2转码成功 1转码失败
        fileInfoMapper.updateFileInfo(info);
    }

}
