package com.netdisk.controller;

import com.netdisk.advice.BusinessException;
import com.netdisk.enums.ResponseCodeEnum;
import com.netdisk.service.FileInfoService;
import com.netdisk.utils.CookieTools;
import com.netdisk.utils.FileTools;
import com.netdisk.utils.StringTools;
import com.netdisk.vo.FileInfoVo;
import com.netdisk.vo.PageFileInfoVo;
import com.netdisk.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/file")
public class FileInfoController {

    @Autowired
    FileInfoService fileInfoService;

    @RequestMapping(value = "updateUserAvatar",method = RequestMethod.POST)
    public ResponseVO updateUserAvatar(HttpServletRequest request, MultipartFile avatar) {
        String avatarFolder=outFileFolder+"/avatar/";//头像文件存储目录
        File folder=new File(avatarFolder);
        if(!folder.exists()){
            // 使用mkdirs而不是mkdir以确保创建所有不存在的父目录
            folder.mkdirs();
        }
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        //没获取到userId
        if (userId==null){
            throw new BusinessException(ResponseCodeEnum.CODE_803);
        }
        File avatarFile = new File(avatarFolder + userId + ".jpg");
        try {
            if(avatarFile.exists()){//存在则先删除
                avatarFile.delete();
            }
            avatar.transferTo(avatarFile);//存储头像
        } catch (Exception e) {
            throw new BusinessException(ResponseCodeEnum.CODE_811);
        }
        return ResponseVO.getSuccessResponseVO(null);
    }

    //更新密码
    @RequestMapping(value = "updatePassword",method = RequestMethod.POST)
    public ResponseVO updatePassword(HttpServletRequest request,String password) {
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.updatePassword(userId,password);
        return ResponseVO.getSuccessResponseVO(null);
    }



    @RequestMapping(value = "loadDataList",method = RequestMethod.POST)
    //获取文件列表
    public ResponseVO loadDataList(HttpServletRequest request,
                                   @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String category,String filePid){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageFileInfoVo pageInfo = fileInfoService.selectPageFileInfo(pageNo,pageSize,category, userId,filePid);
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

    //获取路径path下所有文件以及目录，path多个目录的fileid
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

    //获取所有目录
    @RequestMapping(value = "loadAllFolder",method = RequestMethod.POST)
    public ResponseVO loadAllFolder(HttpServletRequest request,
                                 String filePid,String currentFileIds){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        List<FileInfoVo> fileInfoVo = fileInfoService.loadAllFolder(userId,filePid,currentFileIds);
        return ResponseVO.getSuccessResponseVO(fileInfoVo);
    }

    //批量移动文件fileIds到目录filePid下面
    @RequestMapping(value = "changeFileFolder",method = RequestMethod.POST)
    public ResponseVO changeFileFolder(HttpServletRequest request,
                                    String fileIds,String filePid){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.changeFileFolder(fileIds,userId,filePid);
        return ResponseVO.getSuccessResponseVO(null);
    }

    @RequestMapping("createDownloadUrl/{fileId}")
    public ResponseVO createDownloadUrl(@PathVariable String fileId){
        String downloadToken = fileInfoService.createDownloadToken(fileId);
        return ResponseVO.getSuccessResponseVO(downloadToken);
    }

    @RequestMapping("download/{downloadToken}")
    public void download(HttpServletRequest request, HttpServletResponse response,
                               @PathVariable String downloadToken){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.downloadFile(request,response,downloadToken,userId);
    }

    //批量删除文件
    @RequestMapping(value = "delFile",method = RequestMethod.POST)
    public ResponseVO deleteFile(HttpServletRequest request, HttpServletResponse response,
                                 String fileIds){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        fileInfoService.deleteFile(request,response,userId,fileIds);
        return ResponseVO.getSuccessResponseVO(null);
    }
}
