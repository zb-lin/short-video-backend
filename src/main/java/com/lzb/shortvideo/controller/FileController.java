package com.lzb.shortvideo.controller;

import cn.hutool.core.io.FileUtil;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.constant.FileConstant;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.manager.CosManager;
import com.lzb.shortvideo.model.dto.file.UploadFileRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.enums.FileUploadBizEnum;
import com.lzb.shortvideo.model.vo.UploadFileVo;
import com.lzb.shortvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    private static final List<String> PERMISSIBLE_FILE_FORMATS =
            Arrays.asList("jpeg", "jpg", "svg", "png", "webp",
                    ".avi", ".mp4", ".mkv", ".mov", ".wmv", ".flv", ".rmvb", ".mpeg", ".3gp", ".mpg");

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<UploadFileVo> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                                 UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        String thumbnailPath = String.format("%s/%s/%s.jpg", FileUploadBizEnum.THUMBNAIL, userId, filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file, thumbnailPath);
            // 返回可访问地址
            UploadFileVo uploadFileVo = new UploadFileVo(FileConstant.COS_HOST + filepath, FileConstant.COS_HOST + thumbnailPath);
            return ResultUtils.success(uploadFileVo);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_G = 1024 * 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_G) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1G");
            }
            if (!PERMISSIBLE_FILE_FORMATS.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
