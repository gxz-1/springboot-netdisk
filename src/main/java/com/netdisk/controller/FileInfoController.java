package com.netdisk.controller;

import com.github.pagehelper.PageInfo;
import com.netdisk.advice.BusinessException;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.pojo.FileInfo;
import com.netdisk.service.FileInfoService;
import com.netdisk.service.impl.FileInfoServiceImpl;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.netdisk.utils.FileTools;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/file")
public class FileInfoController {

    @Autowired
    FileInfoService fileInfoService;

    @RequestMapping(value = "loadDataList",method = RequestMethod.POST)
    //获取文件列表
    public ResponseVO loadDataList(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String category,String filePid){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageInfo<FileInfoVo> pageInfo = fileInfoService.selectPageFileInfo(pageNo,pageSize,category, userId,filePid);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }

    @RequestMapping(value = "uploadFile",method = RequestMethod.POST)
    //上传文件
    //前端进行了Md5校验和文件分片，传给后端MD5值fileMd5，以及分片索引chunkIndex，分片总数chunks
    public ResponseVO uploadFile(HttpServletRequest request,HttpServletResponse response,
                                 String fileId, MultipartFile file,String fileName,String filePid,
                                 String fileMd5,Integer chunkIndex,Integer chunks){
        Map result = fileInfoService.uploadFile(request,response,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
        return ResponseVO.getSuccessResponseVO(result);
    }

    @Value("${my.outFileFolder}")
    String outFileFolder;

    //获取缩略图
    @RequestMapping("getImage/{coverName}")//匹配任何字符直到URL的结束（由于路径本身包含斜杠/）
    public void getImage(HttpServletResponse response,
            @PathVariable String coverName){
        if (StringTools.isEmpty(coverName) || StringUtils.isBlank(coverName)) {
            return ;
        }
        String suffix = StringTools.getFileSuffix(coverName);
        String contentType = "image/" + suffix.replace(".", "");//"image/jpg"
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        FileTools.readFile(response, outFileFolder+"/file/"+coverName);
    }

    //读取视频文件
    @RequestMapping("ts/getVideoInfo/{fileId}")
    public void getVideoInfo(HttpServletRequest request,HttpServletResponse response,
                                   @PathVariable String fileId){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.getVideoInfo(response,fileId,userId);
    }

    //读取其他文件
    @RequestMapping("getFile/{fileId}")
    public void getFileInfo(HttpServletRequest request,HttpServletResponse response,
                                  @PathVariable String fileId){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.getFileInfo(response,fileId,userId);
    }

    //新建目录
    @RequestMapping(value = "newFolder",method = RequestMethod.POST)
    public ResponseVO createFolder(HttpServletRequest request,
                                   String filePid,String fileName){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        FileInfoVo fileInfoVo = fileInfoService.createFolder(filePid,userId,fileName);
        return ResponseVO.getSuccessResponseVO(fileInfoVo);
    }

    //获取文件目录，path按层次传入多个目录的fileid
    @RequestMapping(value = "getFolderInfo",method = RequestMethod.POST)
    public ResponseVO getFolderInfo(HttpServletRequest request,String path){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        List<FileInfoVo> res = fileInfoService.getFolderInfo(path,userId);
        return ResponseVO.getSuccessResponseVO(res);
    }

    //重命名
    @RequestMapping(value = "rename",method = RequestMethod.POST)
    public ResponseVO fileRename(HttpServletRequest request,
                                 String fileId,String fileName){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        FileInfoVo fileInfoVo = fileInfoService.fileRename(fileId,userId,fileName);
        return ResponseVO.getSuccessResponseVO(fileInfoVo);
    }

    //获取当前层级filePid下除选中currentFileIds以外的所有目录,用于批量移动currentFileIds
    @RequestMapping(value = "loadAllFolder",method = RequestMethod.POST)
    public ResponseVO loadAllFolder(HttpServletRequest request,
                                 String filePid,String currentFileIds){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        List<FileInfoVo> fileInfoVo = fileInfoService.loadAllFolder(userId,filePid,currentFileIds);
        return ResponseVO.getSuccessResponseVO(fileInfoVo);
    }
}
