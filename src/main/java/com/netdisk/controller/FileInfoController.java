package com.netdisk.controller;

import com.github.pagehelper.PageInfo;
import com.netdisk.enums.FileCategoryEnums;
import com.netdisk.service.FileInfoService;
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

import java.util.Map;


@RestController
@RequestMapping("api/file")
public class FileInfoController {

    @Autowired
    FileInfoService fileInfoService;

    @RequestMapping(value = "loadDataList",method = RequestMethod.POST)
    public ResponseVO loadDataList(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "15") Integer pageSize,
                                   String category){
        String userId = CookieTools.getCookieValue(request, null, "userId", false);
        PageInfo<FileInfoVo> pageInfo = fileInfoService.selectPageFileInfo(pageNo,pageSize,category, userId);
        return ResponseVO.getSuccessResponseVO(pageInfo);
    }

    @RequestMapping(value = "uploadFile",method = RequestMethod.POST)
    //前端进行了Md5校验和文件分片，传给后端MD5值fileMd5，以及分片索引chunkIndex，分片总数chunks
    public ResponseVO uploadFile(HttpServletRequest request,HttpServletResponse response,
                                 String fileId, MultipartFile file,String fileName,String filePid,
                                 String fileMd5,Integer chunkIndex,Integer chunks){

        Map result = fileInfoService.uploadFile(request,response,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
        return ResponseVO.getSuccessResponseVO(result);
    }

    @Value("${my.outFileFolder}")
    String outFileFolder;

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
}
